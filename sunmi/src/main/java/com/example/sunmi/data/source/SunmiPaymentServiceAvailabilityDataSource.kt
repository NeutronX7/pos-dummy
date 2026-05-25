package com.example.sunmi.data.source

internal fun interface SunmiPaymentServiceAvailabilityDataSource {
    fun isInstalled(): Boolean
}
