package com.example.pos_dummy.presentation.viewmodel

import com.example.sunmi.domain.model.ContactlessUiState
import com.example.sunmi.domain.model.ContactlessPaymentUiState
import com.example.sunmi.domain.model.PrinterInfo

data class HomeUiState(
    val printerInfo: PrinterInfo,
    val lastMessage: String = "Waiting to initialize Sunmi dummy flow.",
    val printedReceipt: String = "",
    val simulateOutOfPaper: Boolean = false,
    val contactlessUiState: ContactlessUiState = ContactlessUiState(),
    val paymentUiState: ContactlessPaymentUiState = ContactlessPaymentUiState(),
)
