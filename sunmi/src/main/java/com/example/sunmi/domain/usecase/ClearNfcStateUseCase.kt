package com.example.sunmi.domain.usecase

import com.example.sunmi.domain.model.CardReaderStage
import com.example.sunmi.domain.model.NfcUiState

class ClearNfcStateUseCase {
    operator fun invoke(currentState: NfcUiState): NfcUiState {
        return currentState.copy(
            isReading = false,
            result = currentState.result.copy(
                stage = CardReaderStage.IDLE,
                title = "NFC idle",
                message = "Ready to simulate a Sunmi V2s NFC card read.",
                cardLabel = "",
                cardId = "",
                technology = ""
            )
        )
    }
}
