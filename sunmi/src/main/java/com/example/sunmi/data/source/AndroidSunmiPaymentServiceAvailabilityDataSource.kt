package com.example.sunmi.data.source

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.sunmi.data.model.SunmiServiceConfig

internal class AndroidSunmiPaymentServiceAvailabilityDataSource(
    context: Context,
) : SunmiPaymentServiceAvailabilityDataSource {
    private val appContext = context.applicationContext

    override fun isInstalled(): Boolean {
        val intent = Intent(SunmiServiceConfig.paymentServiceIntentAction)
            .setPackage(SunmiServiceConfig.paymentServicePackage)
        val resolved = appContext.packageManager.queryIntentServices(intent, 0)
        if (!resolved.isNullOrEmpty()) {
            return true
        }

        return try {
            appContext.packageManager.getPackageInfo(SunmiServiceConfig.paymentServicePackage, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
