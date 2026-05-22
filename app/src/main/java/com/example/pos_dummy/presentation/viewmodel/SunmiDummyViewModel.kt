package com.example.pos_dummy.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.pos_dummy.data.repository.FakeSunmiPrinterRepository
import com.example.pos_dummy.domain.model.ContactlessPaymentListener
import com.example.pos_dummy.domain.model.ContactlessPaymentResult
import com.example.pos_dummy.domain.model.ContactlessPaymentStage
import com.example.pos_dummy.domain.model.ContactlessProbeResult
import com.example.pos_dummy.domain.model.ContactlessProbeStage
import com.example.pos_dummy.domain.repository.ContactlessDiagnosticsRepository
import com.example.pos_dummy.domain.repository.ContactlessPaymentRepository
import com.example.pos_dummy.domain.repository.PosPrinterRepository
import com.example.pos_dummy.domain.usecase.contactless.CancelContactlessPaymentUseCase
import com.example.pos_dummy.domain.usecase.contactless.ClearContactlessStateUseCase
import com.example.pos_dummy.domain.usecase.contactless.GetContactlessCapabilitiesUseCase
import com.example.pos_dummy.domain.usecase.contactless.GetContactlessPaymentAvailabilityUseCase
import com.example.pos_dummy.domain.usecase.contactless.StartContactlessProbeUseCase
import com.example.pos_dummy.domain.usecase.contactless.StartContactlessPaymentUseCase

class SunmiDummyViewModel(
    private val printerRepository: PosPrinterRepository = FakeSunmiPrinterRepository(),
    private val contactlessDiagnosticsRepository: ContactlessDiagnosticsRepository,
    private val contactlessPaymentRepository: ContactlessPaymentRepository,
    private val getContactlessCapabilitiesUseCase: GetContactlessCapabilitiesUseCase =
        GetContactlessCapabilitiesUseCase(contactlessDiagnosticsRepository),
    private val getContactlessPaymentAvailabilityUseCase: GetContactlessPaymentAvailabilityUseCase =
        GetContactlessPaymentAvailabilityUseCase(contactlessPaymentRepository),
    private val startContactlessProbeUseCase: StartContactlessProbeUseCase = StartContactlessProbeUseCase(),
    private val startContactlessPaymentUseCase: StartContactlessPaymentUseCase =
        StartContactlessPaymentUseCase(contactlessPaymentRepository),
    private val cancelContactlessPaymentUseCase: CancelContactlessPaymentUseCase =
        CancelContactlessPaymentUseCase(contactlessPaymentRepository),
    private val clearContactlessStateUseCase: ClearContactlessStateUseCase = ClearContactlessStateUseCase(),
) {
    init {
        contactlessPaymentRepository.setListener(
            ContactlessPaymentListener { result ->
                onPaymentEvent(result)
            }
        )
    }

    var uiState by mutableStateOf(
        SunmiDummyUiState(
            printerInfo = printerRepository.getPrinterInfo(),
            contactlessUiState = clearContactlessStateUseCase(
                SunmiDummyUiState(printerInfo = printerRepository.getPrinterInfo()).contactlessUiState.copy(
                    capabilities = getContactlessCapabilitiesUseCase()
                )
            ),
            paymentUiState = SunmiDummyUiState(
                printerInfo = printerRepository.getPrinterInfo()
            ).paymentUiState.copy(
                availability = getContactlessPaymentAvailabilityUseCase()
            ),
        )
    )

    fun refreshContactlessCapabilities() {
        val capabilities = getContactlessCapabilitiesUseCase()
        val paymentAvailability = getContactlessPaymentAvailabilityUseCase()
        uiState = uiState.copy(
            contactlessUiState = clearContactlessStateUseCase(
                uiState.contactlessUiState.copy(capabilities = capabilities)
            ),
            paymentUiState = uiState.paymentUiState.copy(
                availability = paymentAvailability
            ),
            lastMessage = capabilities.statusSummary,
        )
    }

    fun startContactlessProbe() {
        val result = startContactlessProbeUseCase(uiState.contactlessUiState.capabilities)
        val shouldListen = result.stage == ContactlessProbeStage.SEARCHING

        uiState = uiState.copy(
            lastMessage = result.message,
            contactlessUiState = uiState.contactlessUiState.copy(
                isListening = shouldListen,
                result = result
            )
        )
    }

    fun onContactlessTagDetected(
        uid: String,
        technologies: String,
        supportsIsoDep: Boolean,
    ) {
        uiState = uiState.copy(
            lastMessage = "Contactless radio detected a nearby card or phone.",
            contactlessUiState = uiState.contactlessUiState.copy(
                isListening = false,
                result = ContactlessProbeResult(
                    stage = ContactlessProbeStage.DETECTED,
                    title = "Contactless signal detected",
                    message = if (supportsIsoDep) {
                        "The device detected an ISO-DEP capable contactless target. This is a good pre-check before wiring the SUNMI payment SDK."
                    } else {
                        "The device detected a contactless target, but it did not report ISO-DEP support."
                    },
                    uid = uid,
                    technologies = technologies,
                    supportsIsoDep = supportsIsoDep,
                )
            )
        )
    }

    fun clearContactlessState() {
        uiState = uiState.copy(
            contactlessUiState = clearContactlessStateUseCase(uiState.contactlessUiState)
        )
    }

    fun startContactlessPayment() {
        val result = startContactlessPaymentUseCase(uiState.paymentUiState.request)
        uiState = uiState.copy(
            lastMessage = result.message,
            paymentUiState = uiState.paymentUiState.copy(
                isInProgress = result.stage == ContactlessPaymentStage.WAITING_FOR_CARD ||
                    result.stage == ContactlessPaymentStage.PROCESSING,
                result = result
            )
        )
    }

    fun cancelContactlessPayment() {
        val result = cancelContactlessPaymentUseCase()
        uiState = uiState.copy(
            lastMessage = result.message,
            paymentUiState = uiState.paymentUiState.copy(
                isInProgress = false,
                result = result
            )
        )
    }

    fun dispose() {
        contactlessPaymentRepository.dispose()
    }

    private fun SunmiDummyUiState.refreshPrinterInfo(): SunmiDummyUiState {
        return copy(printerInfo = printerRepository.getPrinterInfo())
    }

    private fun onPaymentEvent(result: ContactlessPaymentResult) {
        uiState = uiState.copy(
            lastMessage = result.message,
            paymentUiState = uiState.paymentUiState.copy(
                isInProgress = result.stage == ContactlessPaymentStage.INITIALIZING ||
                    result.stage == ContactlessPaymentStage.WAITING_FOR_CARD ||
                    result.stage == ContactlessPaymentStage.PROCESSING,
                result = result
            )
        )
    }
}
