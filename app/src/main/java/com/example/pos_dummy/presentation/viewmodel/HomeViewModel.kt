package com.example.pos_dummy.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sunmi.domain.model.ContactlessPaymentListener
import com.example.sunmi.domain.model.ContactlessPaymentResult
import com.example.sunmi.domain.model.ContactlessPaymentStage
import com.example.sunmi.domain.model.ContactlessPaymentUiState
import com.example.sunmi.domain.model.ContactlessProbeResult
import com.example.sunmi.domain.model.ContactlessProbeStage
import com.example.sunmi.domain.model.ContactlessUiState
import com.example.sunmi.domain.usecase.GetPrinterInfoUseCase
import com.example.sunmi.domain.usecase.contactless.CancelContactlessPaymentUseCase
import com.example.sunmi.domain.usecase.contactless.ClearContactlessStateUseCase
import com.example.sunmi.domain.usecase.contactless.DisposeContactlessPaymentUseCase
import com.example.sunmi.domain.usecase.contactless.GetContactlessCapabilitiesUseCase
import com.example.sunmi.domain.usecase.contactless.GetContactlessPaymentAvailabilityUseCase
import com.example.sunmi.domain.usecase.contactless.ObserveContactlessPaymentEventsUseCase
import com.example.sunmi.domain.usecase.contactless.StartContactlessProbeUseCase
import com.example.sunmi.domain.usecase.contactless.StartContactlessPaymentUseCase

class HomeViewModel(
    private val getPrinterInfoUseCase: GetPrinterInfoUseCase,
    private val getContactlessCapabilitiesUseCase: GetContactlessCapabilitiesUseCase,
    private val getContactlessPaymentAvailabilityUseCase: GetContactlessPaymentAvailabilityUseCase,
    private val startContactlessProbeUseCase: StartContactlessProbeUseCase,
    private val startContactlessPaymentUseCase: StartContactlessPaymentUseCase,
    private val cancelContactlessPaymentUseCase: CancelContactlessPaymentUseCase,
    private val clearContactlessStateUseCase: ClearContactlessStateUseCase,
    private val observeContactlessPaymentEventsUseCase: ObserveContactlessPaymentEventsUseCase,
    private val disposeContactlessPaymentUseCase: DisposeContactlessPaymentUseCase,
) {
    init {
        observeContactlessPaymentEventsUseCase(ContactlessPaymentListener(::onPaymentEvent))
    }

    var uiState by mutableStateOf(buildInitialUiState())

    fun refreshContactlessCapabilities() {
        val capabilities = getContactlessCapabilitiesUseCase()
        val paymentAvailability = getContactlessPaymentAvailabilityUseCase()
        updateState {
            copy(
                contactlessUiState = clearContactlessStateUseCase(
                    contactlessUiState.copy(capabilities = capabilities)
                ),
                paymentUiState = paymentUiState.copy(availability = paymentAvailability),
                lastMessage = capabilities.statusSummary,
            )
        }
    }

    fun startContactlessProbe() {
        val result = startContactlessProbeUseCase(uiState.contactlessUiState.capabilities)
        applyContactlessResult(
            result = result,
            isListening = result.stage == ContactlessProbeStage.SEARCHING,
        )
    }

    fun onContactlessTagDetected(
        uid: String,
        technologies: String,
        supportsIsoDep: Boolean,
    ) {
        applyContactlessResult(
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
            ),
            isListening = false,
            lastMessage = "Contactless radio detected a nearby card or phone.",
        )
    }

    fun clearContactlessState() {
        updateState {
            copy(contactlessUiState = clearContactlessStateUseCase(contactlessUiState))
        }
    }

    fun startContactlessPayment() {
        applyPaymentResult(startContactlessPaymentUseCase(uiState.paymentUiState.request))
    }

    fun cancelContactlessPayment() {
        applyPaymentResult(cancelContactlessPaymentUseCase())
    }

    fun dispose() {
        disposeContactlessPaymentUseCase()
    }

    private fun buildInitialUiState(): HomeUiState {
        val printerInfo = getPrinterInfoUseCase()
        val capabilities = getContactlessCapabilitiesUseCase()
        val paymentAvailability = getContactlessPaymentAvailabilityUseCase()

        return HomeUiState(
            printerInfo = printerInfo,
            contactlessUiState = clearContactlessStateUseCase(
                ContactlessUiState().copy(capabilities = capabilities)
            ),
            paymentUiState = ContactlessPaymentUiState().copy(
                availability = paymentAvailability
            ),
        )
    }

    private fun applyContactlessResult(
        result: ContactlessProbeResult,
        isListening: Boolean,
        lastMessage: String = result.message,
    ) {
        updateState {
            copy(
                lastMessage = lastMessage,
                contactlessUiState = contactlessUiState.copy(
                    isListening = isListening,
                    result = result,
                ),
            )
        }
    }

    private fun applyPaymentResult(result: ContactlessPaymentResult) {
        updateState {
            copy(
                lastMessage = result.message,
                paymentUiState = paymentUiState.copy(
                    isInProgress = result.stage.isInProgress(),
                    result = result,
                ),
            )
        }
    }

    private fun updateState(transform: HomeUiState.() -> HomeUiState) {
        uiState = uiState.transform()
    }

    private fun onPaymentEvent(result: ContactlessPaymentResult) {
        applyPaymentResult(result)
    }

    private fun ContactlessPaymentStage.isInProgress(): Boolean {
        return this == ContactlessPaymentStage.INITIALIZING ||
            this == ContactlessPaymentStage.WAITING_FOR_CARD ||
            this == ContactlessPaymentStage.PROCESSING
    }
}
