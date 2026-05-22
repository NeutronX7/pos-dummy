package com.example.pos_dummy.data.repository

import com.example.pos_dummy.data.model.SunmiServiceConfig
import com.example.pos_dummy.domain.model.PrintResult
import com.example.pos_dummy.domain.model.PrinterInfo
import com.example.pos_dummy.domain.model.ReceiptDraft
import com.example.pos_dummy.domain.model.ReceiptLine
import com.example.pos_dummy.domain.repository.PosPrinterRepository

class FakeSunmiPrinterRepository : PosPrinterRepository {
    private var connected = false
    private var simulateOutOfPaper = false

    override fun connect(): String {
        connected = true
        return "Connected to Sunmi dummy service. No hardware is required for this flow."
    }

    override fun getPrinterInfo(): PrinterInfo {
        return PrinterInfo(
            vendor = "SUNMI",
            deviceFamily = "Handheld POS / P-series style setup",
            transport = "Built-in print service",
            servicePackage = SunmiServiceConfig.servicePackage,
            serviceAction = SunmiServiceConfig.serviceAction,
            recommendedDependency = SunmiServiceConfig.printLibrary,
            sdkTrack = "Remote dependency first, AIDL fallback",
            paperWidth = "58mm handheld profile",
            status = when {
                !connected -> "Disconnected"
                simulateOutOfPaper -> "Out of paper"
                else -> "Ready"
            },
            notes = SunmiServiceConfig.setupChecklist + listOf(
                "Handheld AIDL resources must match the actual device family."
            )
        )
    }

    override fun buildSampleReceipt(): ReceiptDraft {
        return ReceiptDraft(
            title = "Dummy Coffee Shop",
            subtitle = "Sunmi integration sandbox",
            orderId = "ORD-10027",
            lines = listOf(
                ReceiptLine("Latte", "4.50"),
                ReceiptLine("Bagel", "2.25"),
                ReceiptLine("Tax", "0.81")
            ),
            total = "7.56",
            footer = "This receipt was rendered by a fake Sunmi printer."
        )
    }

    override fun printReceipt(receipt: ReceiptDraft): PrintResult {
        if (!connected) {
            return PrintResult(
                success = false,
                message = "Printer is not connected. Bind the Sunmi service first.",
                renderedReceipt = ""
            )
        }

        if (simulateOutOfPaper) {
            return PrintResult(
                success = false,
                message = "Simulated printer state: out of paper.",
                renderedReceipt = ""
            )
        }

        val renderedReceipt = buildString {
            appendLine(receipt.title)
            appendLine(receipt.subtitle)
            appendLine("Order: ${receipt.orderId}")
            appendLine("--------------------------------")
            receipt.lines.forEach { line ->
                if (line.amount == null) {
                    appendLine(line.label)
                } else {
                    appendLine("${line.label.padEnd(24, ' ')}${line.amount}")
                }
            }
            appendLine("--------------------------------")
            appendLine("${"TOTAL".padEnd(24, ' ')}${receipt.total}")
            appendLine(receipt.footer)
        }

        return PrintResult(
            success = true,
            message = "Dummy print completed. The receipt below is what your Sunmi flow would send.",
            renderedReceipt = renderedReceipt
        )
    }

    override fun setSimulateOutOfPaper(value: Boolean) {
        simulateOutOfPaper = value
    }
}
