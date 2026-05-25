package com.example.sunmi.domain.usecase

import com.example.sunmi.domain.model.CardEntryType
import com.example.sunmi.domain.model.CardReaderResult
import com.example.sunmi.domain.model.CardReaderStage
import com.example.sunmi.domain.model.NfcReadScenario
import com.example.sunmi.domain.repository.CardEntryRepository

class StartNfcReadUseCase(
    private val cardEntryRepository: CardEntryRepository,
) {
    fun createSearchingState(): CardReaderResult {
        return CardReaderResult(
            entryType = CardEntryType.NFC,
            stage = CardReaderStage.SEARCHING,
            title = "Searching for NFC card",
            message = "Waiting for a card near the Sunmi V2s NFC area."
        )
    }

    suspend operator fun invoke(scenario: NfcReadScenario): CardReaderResult {
        return cardEntryRepository.readNfc(scenario)
    }
}
