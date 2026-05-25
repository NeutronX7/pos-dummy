package com.example.sunmi.domain.usecase.contactless

import com.example.sunmi.domain.model.ContactlessPaymentResult
import com.example.sunmi.domain.repository.ContactlessPaymentRepository

class CancelContactlessPaymentUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(): ContactlessPaymentResult {
        return repository.cancelPayment()
    }
}
