package com.example.pos_dummy.domain.usecase.contactless

import com.example.pos_dummy.domain.model.ContactlessPaymentRequest
import com.example.pos_dummy.domain.model.ContactlessPaymentResult
import com.example.pos_dummy.domain.repository.ContactlessPaymentRepository

class StartContactlessPaymentUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(request: ContactlessPaymentRequest): ContactlessPaymentResult {
        return repository.startPayment(request)
    }
}
