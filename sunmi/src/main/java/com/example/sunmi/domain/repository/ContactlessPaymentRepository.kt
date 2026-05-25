package com.example.sunmi.domain.repository

import com.example.sunmi.domain.model.ContactlessPaymentAvailability
import com.example.sunmi.domain.listener.ContactlessPaymentListener
import com.example.sunmi.domain.model.ContactlessPaymentRequest
import com.example.sunmi.domain.model.ContactlessPaymentResult

interface ContactlessPaymentRepository {
    fun getAvailability(): ContactlessPaymentAvailability

    fun setListener(listener: ContactlessPaymentListener?)

    fun startPayment(request: ContactlessPaymentRequest): ContactlessPaymentResult

    fun cancelPayment(): ContactlessPaymentResult

    fun dispose()
}
