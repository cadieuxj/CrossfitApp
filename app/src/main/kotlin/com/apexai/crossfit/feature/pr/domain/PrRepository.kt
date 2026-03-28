package com.apexai.crossfit.feature.pr.domain

import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import kotlinx.coroutines.flow.Flow

interface PrRepository {
    fun getAllPrs(userId: String): Flow<Map<String, List<PersonalRecord>>>
    fun getPrHistory(userId: String, movementId: String): Flow<List<PrHistoryEntry>>
}
