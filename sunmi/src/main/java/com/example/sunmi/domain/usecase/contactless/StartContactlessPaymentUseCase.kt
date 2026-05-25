package com.example.sunmi.domain.usecase.contactless

import com.example.sunmi.domain.model.ContactlessPaymentRequest
import com.example.sunmi.domain.model.ContactlessPaymentResult
import com.example.sunmi.domain.repository.ContactlessPaymentRepository

class StartContactlessPaymentUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(request: ContactlessPaymentRequest): ContactlessPaymentResult {
        return repository.startPayment(request)
    }
}
