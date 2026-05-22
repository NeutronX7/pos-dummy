package com.example.pos_dummy.domain.usecase.contactless

import com.example.pos_dummy.domain.model.ContactlessProbeResult
import com.example.pos_dummy.domain.model.ContactlessProbeStage
import com.example.pos_dummy.domain.model.ContactlessUiState

class ClearContactlessStateUseCase {
    operator fun invoke(currentState: ContactlessUiState): ContactlessUiState {
        return currentState.copy(
            isListening = false,
            result = ContactlessProbeResult(
                stage = if (currentState.capabilities.hasNfcFeature) {
                    ContactlessProbeStage.READY
                } else {
                    ContactlessProbeStage.ERROR
                },
                title = if (currentState.capabilities.hasNfcFeature) {
                    "Contactless ready"
                } else {
                    "NFC unavailable"
                },
                message = if (currentState.capabilities.hasNfcFeature) {
                    "Ready to probe the P2-A11 contactless radio."
                } else {
                    "This device does not report NFC hardware availability."
                }
            )
        )
    }
}
