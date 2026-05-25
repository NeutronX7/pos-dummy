package com.example.sunmi.data.repository

import android.content.Context
import com.example.sunmi.data.model.SunmiServiceConfig
import com.example.sunmi.data.source.AndroidContactlessHardwareStatusDataSource
import com.example.sunmi.data.source.ContactlessHardwareStatus
import com.example.sunmi.data.source.ContactlessHardwareStatusDataSource
import com.example.sunmi.domain.model.ContactlessCapabilities
import com.example.sunmi.domain.repository.ContactlessDiagnosticsRepository

class AndroidContactlessDiagnosticsRepository internal constructor(
    private val hardwareStatusDataSource: ContactlessHardwareStatusDataSource,
) : ContactlessDiagnosticsRepository {
    constructor(context: Context) : this(AndroidContactlessHardwareStatusDataSource(context))

    override fun getCapabilities(): ContactlessCapabilities {
        val status = hardwareStatusDataSource.getStatus()
        return ContactlessCapabilities(
            hasNfcFeature = status.hasNfcFeature,
            isNfcEnabled = status.isNfcEnabled,
            readerModeSupported = status.readerModeSupported,
            paymentDeviceHint = "${SunmiServiceConfig.terminalModel} / P-series payment device",
            statusSummary = status.toStatusSummary(),
        )
    }

    private fun ContactlessHardwareStatus.toStatusSummary(): String {
        return when {
            !hasNfcFeature -> "Android does not report a generic NFC feature. On SUNMI payment terminals, contactless may still be available through the vendor payment service."
            !isNfcEnabled -> "NFC hardware found, but Android reports it is disabled."
            else -> "NFC hardware is available and enabled for a contactless probe."
        }
    }
}
