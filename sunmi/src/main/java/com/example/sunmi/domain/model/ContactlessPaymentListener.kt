package com.example.sunmi.domain.model

fun interface ContactlessPaymentListener {
    fun onEvent(result: ContactlessPaymentResult)
}
