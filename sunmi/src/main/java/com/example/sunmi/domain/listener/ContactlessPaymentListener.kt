package com.example.sunmi.domain.listener

import com.example.sunmi.domain.model.ContactlessPaymentResult

fun interface ContactlessPaymentListener {
    fun onEvent(result: ContactlessPaymentResult)
}