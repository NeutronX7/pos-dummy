package com.example.pos_dummy.domain.repository

import com.example.pos_dummy.domain.model.CardReaderResult
import com.example.pos_dummy.domain.model.NfcReadScenario

interface CardEntryRepository {

    suspend fun readNfc(scenario: NfcReadScenario): CardReaderResult

    suspend fun readChip(): CardReaderResult

    suspend fun readMagStripe(): CardReaderResult
}
