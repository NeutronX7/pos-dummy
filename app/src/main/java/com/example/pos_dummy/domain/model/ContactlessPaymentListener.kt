package com.example.pos_dummy.domain.model

fun interface ContactlessPaymentListener {
    fun onEvent(result: ContactlessPaymentResult)
}
