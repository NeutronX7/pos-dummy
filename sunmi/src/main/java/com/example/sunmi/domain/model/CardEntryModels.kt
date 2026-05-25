package com.example.sunmi.domain.model

enum class CardEntryType {
    NFC,
    CHIP,
    MAG_STRIPE,
}

enum class CardReaderStage {
    IDLE,
    SEARCHING,
    CARD_DETECTED,
    SUCCESS,
    ERROR,
}

data class CardReaderResult(
    val entryType: CardEntryType,
    val stage: CardReaderStage,
    val title: String,
    val message: String,
    val cardLabel: String = "",
    val cardId: String = "",
    val technology: String = "",
)
