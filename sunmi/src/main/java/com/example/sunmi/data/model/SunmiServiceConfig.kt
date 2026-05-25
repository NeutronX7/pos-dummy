package com.example.sunmi.data.model

object SunmiServiceConfig {
    const val printLibrary = "com.sunmi:printerlibrary:1.0.15"
    const val servicePackage = "woyou.aidlservice.jiuiv5"
    const val serviceAction = "woyou.aidlservice.jiuiv5.IWoyouService"
    const val paymentServicePackage = "com.sunmi.pay.hardware_v3"
    const val paymentServiceIntentAction = "sunmi.intent.action.PAY_HARDWARE"
    const val terminalModel = "SUNMI P2-A11"

    val setupChecklist = listOf(
        "Use the remote dependency when possible because Sunmi adapts it across terminal models.",
        "Add package visibility for the print service package before binding on newer Sunmi devices.",
        "Treat AIDL as a device-specific fallback when the library path is not available."
    )
}
