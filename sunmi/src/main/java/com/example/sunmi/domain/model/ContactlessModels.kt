package com.example.sunmi.domain.model

enum class ContactlessProbeStage {
    IDLE,
    READY,
    SEARCHING,
    DETECTED,
    ERROR,
}

data class ContactlessCapabilities(
    val hasNfcFeature: Boolean,
    val isNfcEnabled: Boolean,
    val readerModeSupported: Boolean,
    val paymentDeviceHint: String,
    val statusSummary: String,
)

data class ContactlessProbeResult(
    val stage: ContactlessProbeStage,
    val title: String,
    val message: String,
    val uid: String = "",
    val technologies: String = "",
    val supportsIsoDep: Boolean = false,
)

data class ContactlessUiState(
    val capabilities: ContactlessCapabilities = ContactlessCapabilities(
        hasNfcFeature = false,
        isNfcEnabled = false,
        readerModeSupported = true,
        paymentDeviceHint = "P2 payment terminal",
        statusSummary = "Checking NFC hardware status."
    ),
    val isListening: Boolean = false,
    val result: ContactlessProbeResult = ContactlessProbeResult(
        stage = ContactlessProbeStage.IDLE,
        title = "Contactless idle",
        message = "Run a contactless probe to confirm the NFC radio can detect a card or phone."
    ),
)
