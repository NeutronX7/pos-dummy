package com.example.sunmi.domain.model

enum class NfcReadScenario {
    SUCCESS,
    TIMEOUT,
    UNSUPPORTED_CARD,
    READ_ERROR,
    CANCELED,
}

data class NfcUiState(
    val isReading: Boolean = false,
    val selectedScenario: NfcReadScenario = NfcReadScenario.SUCCESS,
    val result: CardReaderResult = CardReaderResult(
        entryType = CardEntryType.NFC,
        stage = CardReaderStage.IDLE,
        title = "NFC idle",
        message = "Ready to simulate a Sunmi V2s NFC card read."
    ),
)
