package com.apexai.crossfit.feature.coach.domain

import com.apexai.crossfit.core.domain.model.CoachConnection
import com.apexai.crossfit.core.domain.model.WorkoutResult
import kotlinx.coroutines.flow.Flow

interface CoachRepository {
    // Athlete-side
    fun getMyCoaches(): Flow<List<CoachConnection>>
    suspend fun linkByCode(inviteCode: String): Result<CoachConnection>
    suspend fun unlinkCoach(connectionId: String): Result<Unit>

    // Coach-side
    fun getMyAthletes(): Flow<List<AthleteInfo>>
    fun getAthleteResults(athleteId: String): Flow<List<WorkoutResult>>
    suspend fun addCoachNote(resultId: String, note: String): Result<Unit>
}

data class AthleteInfo(
    val userId: String,
    val displayName: String,
    val email: String?
)
