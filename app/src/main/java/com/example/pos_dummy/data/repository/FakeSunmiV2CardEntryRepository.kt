package com.example.pos_dummy.data.repository

import com.example.pos_dummy.domain.model.CardEntryType
import com.example.pos_dummy.domain.model.CardReaderResult
import com.example.pos_dummy.domain.model.CardReaderStage
import com.example.pos_dummy.domain.model.NfcReadScenario
import com.example.pos_dummy.domain.repository.CardEntryRepository
import kotlinx.coroutines.delay

class FakeSunmiV2CardEntryRepository : CardEntryRepository {
    override suspend fun readNfc(scenario: NfcReadScenario): CardReaderResult {
        delay(1200)
        return when (scenario) {
            NfcReadScenario.SUCCESS -> CardReaderResult(
                entryType = CardEntryType.NFC,
                stage = CardReaderStage.SUCCESS,
                title = "NFC card detected",
                message = "Sunmi V2s dummy flow completed a contactless read simulation.",
                cardLabel = "Membership / Demo Card",
                cardId = "04:A7:3C:91:2B",
                technology = "ISO14443-A"
            )
            NfcReadScenario.TIMEOUT -> CardReaderResult(
                entryType = CardEntryType.NFC,
                stage = CardReaderStage.ERROR,
                title = "Read timeout",
                message = "No NFC card was presented before the read window expired."
            )
            NfcReadScenario.UNSUPPORTED_CARD -> CardReaderResult(
                entryType = CardEntryType.NFC,
                stage = CardReaderStage.ERROR,
                title = "Unsupported card",
                message = "The detected NFC card technology is not supported by this dummy flow."
            )
            NfcReadScenario.READ_ERROR -> CardReaderResult(
                entryType = CardEntryType.NFC,
                stage = CardReaderStage.ERROR,
                title = "Reader error",
                message = "The Sunmi V2s NFC reader reported a simulated read failure."
            )
            NfcReadScenario.CANCELED -> CardReaderResult(
                entryType = CardEntryType.NFC,
                stage = CardReaderStage.ERROR,
                title = "Read canceled",
                message = "The operator canceled the NFC read before a card was detected."
            )
        }
    }

    override suspend fun readChip(): CardReaderResult {
        delay(600)
        return CardReaderResult(
            entryType = CardEntryType.CHIP,
            stage = CardReaderStage.ERROR,
            title = "Chip reader unavailable",
            message = "Sunmi V2s NFC module is being built first. Chip flow is still a placeholder."
        )
    }

    override suspend fun readMagStripe(): CardReaderResult {
        delay(600)
        return CardReaderResult(
            entryType = CardEntryType.MAG_STRIPE,
            stage = CardReaderStage.ERROR,
            title = "Magstripe reader unavailable",
            message = "Sunmi V2s NFC module is being built first. Magstripe flow is still a placeholder."
        )
    }
}
