package com.example.pos_dummy.domain.model

enum class ContactlessPaymentStage {
    IDLE,
    UNAVAILABLE,
    READY,
    INITIALIZING,
    WAITING_FOR_CARD,
    CARD_DETECTED,
    PROCESSING,
    APPROVED,
    DECLINED,
    CANCELED,
    ERROR,
}

data class ContactlessPaymentAvailability(
    val isHardwareReady: Boolean,
    val isVendorSdkConfigured: Boolean,
    val terminalModel: String,
    val statusSummary: String,
)

data class ContactlessPaymentRequest(
    val amountLabel: String,
    val currencyCode: String,
)

data class ContactlessPaymentResult(
    val stage: ContactlessPaymentStage,
    val title: String,
    val message: String,
    val reference: String = "",
)

data class ContactlessPaymentUiState(
    val availability: ContactlessPaymentAvailability = ContactlessPaymentAvailability(
        isHardwareReady = false,
        isVendorSdkConfigured = false,
        terminalModel = "SUNMI P2-A11",
        statusSummary = "Checking payment stack availability."
    ),
    val request: ContactlessPaymentRequest = ContactlessPaymentRequest(
        amountLabel = "1.00",
        currencyCode = "USD"
    ),
    val isInProgress: Boolean = false,
    val result: ContactlessPaymentResult = ContactlessPaymentResult(
        stage = ContactlessPaymentStage.IDLE,
        title = "Payment test idle",
        message = "Run the contactless probe first, then wire the SUNMI payment SDK for a live transaction session."
    ),
)
