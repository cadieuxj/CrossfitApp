package com.apexai.crossfit.feature.wod.domain

import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import kotlinx.coroutines.flow.Flow

interface WodRepository {
    fun getWorkouts(query: String?, timeDomain: TimeDomain?): Flow<List<WorkoutSummary>>
    fun getWorkoutById(wodId: String): Flow<Workout>
    fun getWorkoutMovements(wodId: String): Flow<List<WorkoutMovement>>
    suspend fun logResult(result: WorkoutResultInput): Result<WorkoutResult>
    fun getHistory(userId: String): Flow<List<WorkoutResult>>
    fun getTodayWorkout(): Flow<Workout?>
}
