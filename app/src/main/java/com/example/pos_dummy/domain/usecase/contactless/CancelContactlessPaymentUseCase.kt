package com.example.pos_dummy.domain.usecase.contactless

import com.example.pos_dummy.domain.model.ContactlessPaymentResult
import com.example.pos_dummy.domain.repository.ContactlessPaymentRepository

class CancelContactlessPaymentUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(): ContactlessPaymentResult {
        return repository.cancelPayment()
    }
}
