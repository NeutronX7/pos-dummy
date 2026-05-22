package com.example.pos_dummy.domain.repository

import com.example.pos_dummy.domain.model.ContactlessPaymentAvailability
import com.example.pos_dummy.domain.model.ContactlessPaymentListener
import com.example.pos_dummy.domain.model.ContactlessPaymentRequest
import com.example.pos_dummy.domain.model.ContactlessPaymentResult

interface ContactlessPaymentRepository {
    fun getAvailability(): ContactlessPaymentAvailability

    fun setListener(listener: ContactlessPaymentListener?)

    fun startPayment(request: ContactlessPaymentRequest): ContactlessPaymentResult

    fun cancelPayment(): ContactlessPaymentResult

    fun dispose()
}
