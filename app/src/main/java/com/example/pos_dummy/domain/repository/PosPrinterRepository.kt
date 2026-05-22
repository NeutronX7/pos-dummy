package com.example.pos_dummy.domain.repository

import com.example.pos_dummy.domain.model.PrintResult
import com.example.pos_dummy.domain.model.PrinterInfo
import com.example.pos_dummy.domain.model.ReceiptDraft

interface PosPrinterRepository {
    fun connect(): String
    fun getPrinterInfo(): PrinterInfo
    fun buildSampleReceipt(): ReceiptDraft
    fun printReceipt(receipt: ReceiptDraft): PrintResult
    fun setSimulateOutOfPaper(value: Boolean)
}
