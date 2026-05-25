package com.example.sunmi.domain.usecase.contactless

import com.example.sunmi.domain.repository.ContactlessPaymentRepository

class DisposeContactlessPaymentUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke() {
        repository.dispose()
    }
}
