package com.example.pos_dummy.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.RemoteException
import com.example.pos_dummy.domain.model.ContactlessPaymentAvailability
import com.example.pos_dummy.domain.model.ContactlessPaymentListener
import com.example.pos_dummy.domain.model.ContactlessPaymentRequest
import com.example.pos_dummy.domain.model.ContactlessPaymentResult
import com.example.pos_dummy.domain.model.ContactlessPaymentStage
import com.example.pos_dummy.domain.repository.ContactlessDiagnosticsRepository
import com.example.pos_dummy.domain.repository.ContactlessPaymentRepository
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import sunmi.paylib.SunmiPayKernel

class SunmiContactlessPaymentRepository(
    context: Context,
    private val diagnosticsRepository: ContactlessDiagnosticsRepository,
) : ContactlessPaymentRepository {
    private val appContext = context.applicationContext
    private val kernel = SunmiPayKernel.getInstance()
    private var listener: ContactlessPaymentListener? =  null
    private var pendingRequest: ContactlessPaymentRequest? = null
    private var isConnected = false

    private val connectCallback = object : SunmiPayKernel.ConnectCallback {
        override fun onConnectPaySDK() {
            isConnected = true
            listener?.onEvent(
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.READY,
                    title = "SUNMI payment SDK connected",
                    message = "Pay service connected. Starting contactless card detection."
                )
            )
            beginCheckCard()
        }

        override fun onDisconnectPaySDK() {
            isConnected = false
            listener?.onEvent(
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.ERROR,
                    title = "SUNMI payment SDK disconnected",
                    message = "The payment service disconnected before the contactless test completed."
                )
            )
        }
    }

    private val checkCardCallback = object : CheckCardCallbackV2.Stub() {

        override fun findMagCard(info: Bundle?) = Unit

        override fun findICCard(atr: String?) = Unit

        override fun findRFCard(uuid: String?) {
            notifyDetected(uuid.orEmpty(), null, null, null, null)
        }

        override fun onError(code: Int, message: String?) {
            notifyError(code, message)
        }

        override fun findICCardEx(info: Bundle?) = Unit

        override fun findRFCardEx(info: Bundle?) {
            notifyDetected(
                uuid = info?.getString("uuid").orEmpty(),
                ats = info?.getString("ats"),
                cardType = info?.getInt("cardType"),
                cardCategory = info?.getInt("cardCategory"),
                pan = info?.getString("pan")
            )
        }

        override fun onErrorEx(info: Bundle?) {
            notifyError(
                code = info?.getInt("code") ?: Int.MIN_VALUE,
                message = info?.getString("message")
            )
        }
    }

    override fun getAvailability(): ContactlessPaymentAvailability {
        val capabilities = diagnosticsRepository.getCapabilities()
        val serviceInstalled = isPayServiceInstalled()
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
            terminalModel = "SUNMI P2-A11",
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
                message = "The SUNMI payment service package com.sunmi.pay.hardware_v3 was not found on the device."
            )
        }

        return if (kernel.mReadCardOptV2 != null && isConnected) {
            beginCheckCard()
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.WAITING_FOR_CARD,
                title = "Waiting for card",
                message = waitingMessage(request)
            )
        } else {
            val bindStarted = kernel.initPaySDK(appContext, connectCallback)
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
        return try {
            kernel.mReadCardOptV2?.cancelCheckCard()
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.CANCELED,
                title = "Payment canceled",
                message = "The SUNMI contactless check-card session was canceled."
            )
        } catch (error: RemoteException) {
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.ERROR,
                title = "Cancel failed",
                message = "Could not cancel the SUNMI card-detection session: ${error.message.orEmpty()}"
            )
        }
    }

    override fun dispose() {
        kernel.removeConnectCallback(connectCallback)
        try {
            kernel.mReadCardOptV2?.cancelCheckCard()
        } catch (_: RemoteException) {
        }
        kernel.destroyPaySDK()
        isConnected = false
        listener = null
    }

    private fun beginCheckCard() {
        val request = pendingRequest ?: return
        try {
            kernel.mReadCardOptV2?.cancelCheckCard()
            kernel.mReadCardOptV2?.checkCard(
                AidlConstants.CardType.NFC.value,
                checkCardCallback,
                60,
            )
            listener?.onEvent(
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.WAITING_FOR_CARD,
                    title = "Waiting for card",
                    message = waitingMessage(request)
                )
            )
        } catch (error: RemoteException) {
            listener?.onEvent(
                ContactlessPaymentResult(
                    stage = ContactlessPaymentStage.ERROR,
                    title = "Check card failed",
                    message = "SUNMI could not start the NFC card-detection session: ${error.message.orEmpty()}"
                )
            )
        }
    }

    private fun notifyDetected(uuid: String, ats: String?, cardCategory: Int?, cardType: Int?, pan: String?) {
        listener?.onEvent(
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.CARD_DETECTED,
                title = "Contactless card detected",
                message = if (ats.isNullOrBlank()) {
                    "SUNMI payment SDK detected a nearby contactless card."
                } else {
                    "SUNMI payment SDK detected a nearby contactless card and returned ATS data."
                },
                reference = listOfNotNull(
                    uuid.takeIf { it.isNotBlank() }?.let { "UUID: $it" },
                    ats?.takeIf { it.isNotBlank() }?.let { "ATS: $it" },
                    cardCategory?.takeIf { it != null }.let { "cardCategory: $cardCategory" },
                    cardType?.takeIf { it != null }.let { "cardType: $cardType" },
                    pan?.takeIf { it.isNotBlank() }.let { "pan: $pan" }

                ).joinToString(" | ")
            )
        )
    }

    private fun notifyError(code: Int, message: String?) {
        listener?.onEvent(
            ContactlessPaymentResult(
                stage = ContactlessPaymentStage.ERROR,
                title = "Contactless test error",
                message = buildString {
                    append("SUNMI check-card callback returned")
                    if (code != Int.MIN_VALUE) {
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

    private fun isPayServiceInstalled(): Boolean {
        val intent = Intent("sunmi.intent.action.PAY_HARDWARE").setPackage("com.sunmi.pay.hardware_v3")
        val resolved = appContext.packageManager.queryIntentServices(intent, 0)
        if (!resolved.isNullOrEmpty()) {
            return true
        }
        return try {
            appContext.packageManager.getPackageInfo("com.sunmi.pay.hardware_v3", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun waitingMessage(request: ContactlessPaymentRequest): String {
        return "Present a contactless card to start the SUNMI payment test for ${request.currencyCode} ${request.amountLabel}."
    }
}
