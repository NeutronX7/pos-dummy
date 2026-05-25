package com.example.sunmi.domain.usecase.contactless

import com.example.sunmi.domain.model.ContactlessPaymentAvailability
import com.example.sunmi.domain.repository.ContactlessPaymentRepository

class GetContactlessPaymentAvailabilityUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(): ContactlessPaymentAvailability {
        return repository.getAvailability()
    }
}
