package com.example.sunmi.data.source

import android.content.Context
import android.nfc.NfcAdapter
import android.os.Build

internal class AndroidContactlessHardwareStatusDataSource(
    context: Context,
) : ContactlessHardwareStatusDataSource {
    private val appContext = context.applicationContext

    override fun getStatus(): ContactlessHardwareStatus {
        val packageManager = appContext.packageManager
        val adapter = NfcAdapter.getDefaultAdapter(appContext)

        return ContactlessHardwareStatus(
            hasNfcFeature = packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_NFC),
            isNfcEnabled = adapter?.isEnabled == true,
            readerModeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT,
        )
    }
}
