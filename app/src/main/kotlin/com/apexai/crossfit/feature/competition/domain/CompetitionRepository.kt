package com.apexai.crossfit.feature.competition.domain

import com.apexai.crossfit.core.domain.model.CompetitionEvent
import com.apexai.crossfit.core.domain.model.CompetitionStanding
import com.apexai.crossfit.core.domain.model.CompetitionStandingInput
import kotlinx.coroutines.flow.Flow

interface CompetitionRepository {
    fun getEvents(): Flow<List<CompetitionEvent>>
    fun getActiveEvents(): Flow<List<CompetitionEvent>>
    fun getEventById(eventId: String): Flow<CompetitionEvent>
    fun getMyStandings(eventId: String): Flow<List<CompetitionStanding>>
    suspend fun upsertStanding(input: CompetitionStandingInput): Result<CompetitionStanding>
    suspend fun deleteStanding(standingId: String): Result<Unit>
}
