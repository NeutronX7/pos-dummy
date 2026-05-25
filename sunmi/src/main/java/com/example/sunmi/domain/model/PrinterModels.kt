package com.example.sunmi.domain.model

data class PrinterInfo(
    val vendor: String,
    val deviceFamily: String,
    val transport: String,
    val servicePackage: String,
    val serviceAction: String,
    val recommendedDependency: String,
    val sdkTrack: String,
    val paperWidth: String,
    val status: String,
    val notes: List<String>,
)

data class ReceiptLine(
    val label: String,
    val amount: String? = null,
)

data class ReceiptDraft(
    val title: String,
    val subtitle: String,
    val orderId: String,
    val lines: List<ReceiptLine>,
    val total: String,
    val footer: String,
)

data class PrintResult(
    val success: Boolean,
    val message: String,
    val renderedReceipt: String,
)

