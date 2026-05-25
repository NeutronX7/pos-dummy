package com.example.sunmi.domain.usecase.contactless

import com.example.sunmi.domain.listener.ContactlessPaymentListener
import com.example.sunmi.domain.repository.ContactlessPaymentRepository

class ObserveContactlessPaymentEventsUseCase(
    private val repository: ContactlessPaymentRepository,
) {
    operator fun invoke(listener: ContactlessPaymentListener?) {
        repository.setListener(listener)
    }
}
