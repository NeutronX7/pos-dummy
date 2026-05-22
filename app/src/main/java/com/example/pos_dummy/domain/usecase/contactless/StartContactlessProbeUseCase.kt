package com.example.pos_dummy.domain.usecase.contactless

import com.example.pos_dummy.domain.model.ContactlessCapabilities
import com.example.pos_dummy.domain.model.ContactlessProbeResult
import com.example.pos_dummy.domain.model.ContactlessProbeStage

class StartContactlessProbeUseCase {
    operator fun invoke(capabilities: ContactlessCapabilities): ContactlessProbeResult {
        return when {
            !capabilities.hasNfcFeature -> ContactlessProbeResult(
                stage = ContactlessProbeStage.ERROR,
                title = "NFC hardware missing",
                message = "The device does not report an NFC feature, so a contactless probe cannot start."
            )
            !capabilities.isNfcEnabled -> ContactlessProbeResult(
                stage = ContactlessProbeStage.ERROR,
                title = "NFC disabled",
                message = "Enable NFC in Android settings before starting the contactless probe."
            )
            !capabilities.readerModeSupported -> ContactlessProbeResult(
                stage = ContactlessProbeStage.ERROR,
                title = "Reader mode unsupported",
                message = "This Android version does not support NFC reader mode."
            )
            else -> ContactlessProbeResult(
                stage = ContactlessProbeStage.SEARCHING,
                title = "Waiting for contactless card",
                message = "Hold a bank card or phone near the P2-A11 NFC area to verify radio detection."
            )
        }
    }
}
