package com.apexai.crossfit.feature.wod.data

import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrUnit
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.wod.domain.WodRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class WorkoutRow(
    val id: String,
    val name: String,
    val description: String? = null,
    val time_domain: String,
    val scoring_metric: String,
    val time_cap_seconds: Int? = null,
    val rounds: Int? = null,
    val created_at: String
)

@Serializable
data class WorkoutMovementRow(
    val id: String,
    val workout_id: String,
    val movement_id: String,
    val prescribed_reps: Int? = null,
    val prescribed_weight_kg: Double? = null,
    val prescribed_distance_m: Double? = null,
    val prescribed_calories: Int? = null,
    val sort_order: Int
)

@Serializable
data class MovementRow(
    val id: String,
    val name: String,
    val category: String,
    val primary_muscles: List<String> = emptyList(),
    val equipment: String? = null
)

@Serializable
data class ResultRow(
    val id: String,
    val user_id: String,
    val workout_id: String,
    val score: String,
    val score_numeric: Double? = null,
    val rxd: Boolean,
    val notes: String? = null,
    val rpe: Int? = null,
    val completed_at: String
)

@Serializable
data class PersonalRecordRow(
    val id: String,
    val user_id: String,
    val movement_id: String,
    val value: Double,
    val unit: String,
    val achieved_at: String
)

@Singleton
class WodRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WodRepository {

    override fun getWorkouts(query: String?, timeDomain: TimeDomain?): Flow<List<WorkoutSummary>> = flow {
        val rows = supabase.postgrest["workouts"]
            .select {
                if (timeDomain != null) {
                    filter { eq("time_domain", timeDomain.name) }
                }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<WorkoutRow>()

        val filtered = if (!query.isNullOrBlank()) {
            rows.filter { it.name.contains(query, ignoreCase = true) }
        } else rows

        emit(filtered.map { it.toSummary() })
    }

    override fun getWorkoutById(wodId: String): Flow<Workout> = flow {
        val row = supabase.postgrest["workouts"]
            .select { filter { eq("id", wodId) } }
            .decodeSingle<WorkoutRow>()

        val movements = fetchMovementsForWorkout(wodId)
        emit(row.toWorkout(movements))
    }

    override fun getWorkoutMovements(wodId: String): Flow<List<WorkoutMovement>> = flow {
        emit(fetchMovementsForWorkout(wodId))
Continuing from `WodRepositoryImpl.fetchMovementsForWorkout`:
