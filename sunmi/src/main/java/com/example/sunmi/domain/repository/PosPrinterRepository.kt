package com.example.sunmi.domain.repository

import com.example.sunmi.domain.model.PrintResult
import com.example.sunmi.domain.model.PrinterInfo
import com.example.sunmi.domain.model.ReceiptDraft

interface PosPrinterRepository {
    fun connect(): String
    fun getPrinterInfo(): PrinterInfo
    fun buildSampleReceipt(): ReceiptDraft
    fun printReceipt(receipt: ReceiptDraft): PrintResult
    fun setSimulateOutOfPaper(value: Boolean)
}
