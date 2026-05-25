package com.example.sunmi.domain.usecase

import com.example.sunmi.domain.model.PrinterInfo
import com.example.sunmi.domain.repository.PosPrinterRepository

class GetPrinterInfoUseCase(
    private val repository: PosPrinterRepository,
) {
    operator fun invoke(): PrinterInfo {
        return repository.getPrinterInfo()
    }
}
