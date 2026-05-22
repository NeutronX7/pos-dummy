package com.example.pos_dummy.data.model

object SunmiPaymentServiceConfig {
    const val paymentSdkDocsTitle = "Payment SDK for SUNMI's P series devices"
    const val paymentSdkDocsUrl = "https://docs.sunmi.com/en-US/cdixeghjk491/xfdqeghjk513"
    const val paymentSdkUpdatedOn = "May 15, 2026"

    val setupChecklist = listOf(
        "Obtain the SUNMI payment SDK package for the P-series terminal.",
        "Bind or initialize the SUNMI payment service from the data layer only.",
        "Map SUNMI callback events into domain payment states before exposing them to the UI."
    )
}
