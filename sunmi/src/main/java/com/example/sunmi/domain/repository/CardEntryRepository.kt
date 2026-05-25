package com.example.sunmi.domain.repository

import com.example.sunmi.domain.model.CardReaderResult
import com.example.sunmi.domain.model.NfcReadScenario

interface CardEntryRepository {

    suspend fun readNfc(scenario: NfcReadScenario): CardReaderResult

    suspend fun readChip(): CardReaderResult

    suspend fun readMagStripe(): CardReaderResult
}
