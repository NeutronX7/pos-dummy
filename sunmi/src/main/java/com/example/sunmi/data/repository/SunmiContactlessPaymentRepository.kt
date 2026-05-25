package com.example.sunmi.data.repository

import android.content.Context
import com.example.sunmi.data.model.SunmiServiceConfig
import com.example.sunmi.data.source.AndroidSunmiPaymentServiceAvailabilityDataSource
import com.example.sunmi.data.source.PaymentSdkOperationResult
import com.example.sunmi.data.source.SunmiPayKernelDataSource
import com.example.sunmi.data.source.SunmiPaymentSdkDataSource
import com.example.sunmi.data.source.SunmiPaymentSdkListener
import com.example.sunmi.data.source.SunmiPaymentServiceAvailabilityDataSource
import com.example.sunmi.data.source.ContactlessCardDetails
import com.example.sunmi.domain.model.ContactlessPaymentAvailability
import com.example.sunmi.domain.model.ContactlessPaymentListener
import com.example.sunmi.domain.model.ContactlessPaymentRequest
import com.example.sunmi.domain.model.ContactlessPaymentResult
import com.example.sunmi.domain.model.ContactlessPaymentStage
import com.example.sunmi.domain.repository.ContactlessDiagnosticsRepository
import com.example.sunmi.domain.repository.ContactlessPaymentRepository

class SunmiContactlessPaymentRepository internal constructor(
    private val diagnosticsRepository: ContactlessDiagnosticsRepository,
    private val paymentServiceAvailabilityDataSource: SunmiPaymentServiceAvailabilityDataSource,
    private val paymentSdkDataSource: SunmiPaymentSdkDataSource,
) : ContactlessPaymentRepository {
    constructor(
        context: Context,
        diagnosticsRepository: ContactlessDiagnosticsRepository,
    ) : this(
        diagnosticsRepository = diagnosticsRepository,
        paymentServiceAvailabilityDataSource = AndroidSunmiPaymentServiceAvailabilityDataSource(context),
        paymentSdkDataSource = SunmiPayKernelDataSource(context),
    )

    private companion object {
        private const val checkCardTimeoutSeconds = 60
    }

    private var listener: ContactlessPaymentListener? = null
    private var pendingRequest: ContactlessPaymentRequest? = null

    private val sdkListener = object : SunmiPaymentSdkListener {
        override fun onSdkConnected() {
            listener?.onEvent(readyResult())
            beginCheckCard()
        }

        override fun onSdkDisconnected() {
            listener?.onEvent(
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.ERROR,
                    title = "SUNMI payment SDK disconnected",
                    message = "The payment service disconnected before the contactless test completed."
                )
            )
        }

        override fun onCardDetected(cardDetails: ContactlessCardDetails) {
            notifyDetected(cardDetails)
        }

        override fun onError(code: Int?, message: String?) {
            notifyError(code, message)
        }
    }

    override fun getAvailability(): ContactlessPaymentAvailability {
        val capabilities = diagnosticsRepository.getCapabilities()
        val serviceInstalled = paymentServiceAvailabilityDataSource.isInstalled()
        val hardwareReady = serviceInstalled || (capabilities.hasNfcFeature && capabilities.isNfcEnabled)
        val status = when {
            serviceInstalled -> "SUNMI payment service is available. You can start a contactless test through the vendor SDK even if Android does not expose generic NFC."
            !capabilities.hasNfcFeature -> "Android does not report generic NFC and the SUNMI payment service package was not found."
            !capabilities.isNfcEnabled -> "NFC hardware appears disabled and the SUNMI payment service package was not found."
            !serviceInstalled -> "SUNMI payment service package com.sunmi.pay.hardware_v3 was not found on the device."
            else -> "SUNMI payment service is available. You can start a contactless test through the vendor SDK."
        }

        return ContactlessPaymentAvailability(
            isHardwareReady = hardwareReady,
            isVendorSdkConfigured = serviceInstalled,
            terminalModel = SunmiServiceConfig.terminalModel,
            statusSummary = status,
        )
    }

    override fun setListener(listener: ContactlessPaymentListener?) {
        this.listener = listener
    }

    override fun startPayment(request: ContactlessPaymentRequest): ContactlessPaymentResult {
        pendingRequest = request
        val availability = getAvailability()
        if (!availability.isVendorSdkConfigured) {
            return ContactlessPaymentResult(
                stage = ContactlessPaymentStage.UNAVAILABLE,
                title = "Payment service missing",
                message = "The SUNMI payment service package ${SunmiServiceConfig.paymentServicePackage} was not found on the device."
            )
        }

        return if (paymentSdkDataSource.isConnected()) {
            beginCheckCard()
            waitingForCardResult(request)
        } else {
            val bindStarted = paymentSdkDataSource.connect(sdkListener)
            if (bindStarted) {
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.INITIALIZING,
                    title = "Connecting to SUNMI payment SDK",
                    message = "Binding to the SUNMI payment service before starting the contactless test."
                )
            } else {
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.ERROR,
                    title = "Failed to bind payment service",
                    message = "The app could not bind to the SUNMI payment service."
                )
            }
        }
    }

    override fun cancelPayment(): ContactlessPaymentResult {
        return when (val result = paymentSdkDataSource.cancelCardCheck()) {
            PaymentSdkOperationResult.Success -> ContactlessPaymentResult(
                stage = ContactlessPaymentStage.CANCELED,
                title = "Payment canceled",
                message = "The SUNMI contactless check-card session was canceled."
            )
            is PaymentSdkOperationResult.Failure -> ContactlessPaymentResult(
                stage = ContactlessPaymentStage.ERROR,
                title = "Cancel failed",
                message = "Could not cancel the SUNMI card-detection session: ${result.message}"
            )
        }
    }

    override fun dispose() {
        paymentSdkDataSource.disconnect()
        listener = null
    }

    private fun beginCheckCard() {
        val request = pendingRequest ?: return
        when (val result = paymentSdkDataSource.startContactlessCardCheck(checkCardTimeoutSeconds)) {
            PaymentSdkOperationResult.Success -> {
                listener?.onEvent(waitingForCardResult(request))
            }
            is PaymentSdkOperationResult.Failure -> {
                listener?.onEvent(
                    ContactlessPaymentResult(
                        stage = ContactlessPaymentStage.ERROR,
                        title = "Check card failed",
                        message = "SUNMI could not start the NFC card-detection session: ${result.message}"
                    )
                )
            }
        }
    }

    private fun readyResult(): ContactlessPaymentResult {
        return ContactlessPaymentResult(
            stage = ContactlessPaymentStage.READY,
            title = "SUNMI payment SDK connected",
            message = "Pay service connected. Starting contactless card detection."
        )
    }

    private fun waitingForCardResult(request: ContactlessPaymentRequest): ContactlessPaymentResult {
        return ContactlessPaymentResult(
            stage = ContactlessPaymentStage.WAITING_FOR_CARD,
            title = "Waiting for card",
            message = waitingMessage(request)
        )
    }

    private fun notifyDetected(cardDetails: ContactlessCardDetails) {
        listener?.onEvent(
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.CARD_DETECTED,
                title = "Contactless card detected",
                message = if (cardDetails.ats.isNullOrBlank()) {
                    "SUNMI payment SDK detected a nearby contactless card."
                } else {
                    "SUNMI payment SDK detected a nearby contactless card and returned ATS data."
                },
                reference = listOfNotNull(
                    cardDetails.uuid.takeIf { it.isNotBlank() }?.let { "UUID: $it" },
                    cardDetails.ats?.takeIf { it.isNotBlank() }?.let { "ATS: $it" },
                    cardDetails.cardCategory?.let { "cardCategory: $it" },
                    cardDetails.cardType?.let { "cardType: $it" },
                    cardDetails.pan?.takeIf { it.isNotBlank() }?.let { "pan: $it" },
                ).joinToString(" | ")
            )
        )
    }

    private fun notifyError(code: Int?, message: String?) {
        listener?.onEvent(
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.ERROR,
                title = "Contactless test error",
                message = buildString {
                    append("SUNMI check-card callback returned")
                    if (code != null) {
                        append(" code ")
                        append(code)
                    }
                    if (!message.isNullOrBlank()) {
                        append(": ")
                        append(message)
                    }
                }
            )
        )
    }

    private fun waitingMessage(request: ContactlessPaymentRequest): String {
        return "Present a contactless card to start the SUNMI payment test for ${request.currencyCode} ${request.amountLabel}."
    }
}
