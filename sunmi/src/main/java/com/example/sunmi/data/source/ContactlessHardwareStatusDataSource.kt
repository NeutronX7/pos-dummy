package com.example.sunmi.data.source

internal data class ContactlessHardwareStatus(
    val hasNfcFeature: Boolean,
    val isNfcEnabled: Boolean,
    val readerModeSupported: Boolean,
)

internal fun interface ContactlessHardwareStatusDataSource {
    fun getStatus(): ContactlessHardwareStatus
}
