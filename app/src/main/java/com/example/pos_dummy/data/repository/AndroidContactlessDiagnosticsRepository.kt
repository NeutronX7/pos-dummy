package com.example.pos_dummy.data.repository

import android.content.Context
import android.nfc.NfcAdapter
import android.os.Build
import com.example.pos_dummy.domain.model.ContactlessCapabilities
import com.example.pos_dummy.domain.repository.ContactlessDiagnosticsRepository

class AndroidContactlessDiagnosticsRepository(
    context: Context,
) : ContactlessDiagnosticsRepository {
    private val appContext = context.applicationContext

    override fun getCapabilities(): ContactlessCapabilities {
        val packageManager = appContext.packageManager
        val adapter = NfcAdapter.getDefaultAdapter(appContext)
        val hasNfcFeature = packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_NFC)
        val isNfcEnabled = adapter?.isEnabled == true
        val readerModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        val summary = when {
            !hasNfcFeature -> "Android does not report a generic NFC feature. On SUNMI payment terminals, contactless may still be available through the vendor payment service."
            !isNfcEnabled -> "NFC hardware found, but Android reports it is disabled."
            else -> "NFC hardware is available and enabled for a contactless probe."
        }

        return ContactlessCapabilities(
            hasNfcFeature = hasNfcFeature,
            isNfcEnabled = isNfcEnabled,
            readerModeSupported = readerModeSupported,
            paymentDeviceHint = "SUNMI P2-A11 / P-series payment device",
            statusSummary = summary,
        )
    }
}
