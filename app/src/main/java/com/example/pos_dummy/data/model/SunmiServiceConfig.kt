package com.example.pos_dummy.data.model

object SunmiServiceConfig {
    const val printLibrary = "com.sunmi:printerlibrary:1.0.15"
    const val servicePackage = "woyou.aidlservice.jiuiv5"
    const val serviceAction = "woyou.aidlservice.jiuiv5.IWoyouService"

    val setupChecklist = listOf(
        "Use the remote dependency when possible because Sunmi adapts it across terminal models.",
        "Add package visibility for the print service package before binding on newer Sunmi devices.",
        "Treat AIDL as a device-specific fallback when the library path is not available."
    )
}
