package com.example.pos_dummy.data.repository

import com.example.pos_dummy.domain.model.ContactlessPaymentAvailability
import com.example.pos_dummy.domain.model.ContactlessPaymentListener
import com.example.pos_dummy.domain.model.ContactlessPaymentRequest
import com.example.pos_dummy.domain.model.ContactlessPaymentResult
import com.example.pos_dummy.domain.model.ContactlessPaymentStage
import com.example.pos_dummy.domain.repository.ContactlessDiagnosticsRepository
import com.example.pos_dummy.domain.repository.ContactlessPaymentRepository

class StubSunmiContactlessPaymentRepository(
    private val diagnosticsRepository: ContactlessDiagnosticsRepository,
) : ContactlessPaymentRepository {
    override fun setListener(listener: ContactlessPaymentListener?) = Unit

    override fun getAvailability(): ContactlessPaymentAvailability {
        val capabilities = diagnosticsRepository.getCapabilities()
        val hardwareReady = capabilities.hasNfcFeature && capabilities.isNfcEnabled
        val summary = if (hardwareReady) {
            "NFC hardware is ready, but the SUNMI payment SDK is not integrated yet."
        } else {
            "Fix NFC hardware availability first, then integrate the SUNMI payment SDK."
        }

        return ContactlessPaymentAvailability(
            isHardwareReady = hardwareReady,
            isVendorSdkConfigured = false,
            terminalModel = "SUNMI P2-A11",
            statusSummary = summary,
        )
    }

    override fun startPayment(request: ContactlessPaymentRequest): ContactlessPaymentResult {
        val availability = getAvailability()
        return when {
            !availability.isHardwareReady -> ContactlessPaymentResult(
                stage = ContactlessPaymentStage.UNAVAILABLE,
                title = "Hardware not ready",
                message = "The NFC radio is not ready. Complete the contactless probe before starting a payment session."
            )
            !availability.isVendorSdkConfigured -> ContactlessPaymentResult(
                stage = ContactlessPaymentStage.UNAVAILABLE,
                title = "SUNMI payment SDK missing",
                message = "The app can see the NFC hardware, but the SUNMI payment SDK still needs to be added for a real ${request.currencyCode} ${request.amountLabel} contactless transaction."
            )
            else -> ContactlessPaymentResult(
                stage = ContactlessPaymentStage.WAITING_FOR_CARD,
                title = "Waiting for card",
                message = "The payment session is ready for tap."
            )
        }
    }

    override fun cancelPayment(): ContactlessPaymentResult {
        return ContactlessPaymentResult(
            stage = ContactlessPaymentStage.CANCELED,
            title = "Payment canceled",
            message = "No active SUNMI payment session is wired yet, so cancel only clears the test state."
        )
    }

    override fun dispose() = Unit
}
