package com.example.pos_dummy.domain.usecase

import com.example.pos_dummy.domain.model.CardReaderStage
import com.example.pos_dummy.domain.model.NfcUiState

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
