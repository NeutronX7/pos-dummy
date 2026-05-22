package com.example.pos_dummy.domain.usecase.contactless

import com.example.pos_dummy.domain.model.ContactlessPaymentAvailability
import com.example.pos_dummy.domain.repository.ContactlessPaymentRepository

class GetContactlessPaymentAvailabilityUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(): ContactlessPaymentAvailability {
        return repository.getAvailability()
    }
}
