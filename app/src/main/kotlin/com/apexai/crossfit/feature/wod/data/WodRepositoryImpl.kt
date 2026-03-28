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
    val sort_order: Int,
    val rep_scheme: String? = null
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
    val completed_at: String,
    val coach_note: String? = null,
    val is_official_submission: Boolean = false
)

@Serializable
data class PersonalRecordRow(
    val id: String,
    val user_id: String,
    val movement_id: String,
    val movement_name: String? = null,   // populated by join when querying post-log PRs
    val movement_category: String? = null,
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
    }

    override suspend fun logResult(result: WorkoutResultInput): Result<WorkoutResult> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not authenticated"))

            val now = Instant.now()
            val resultRow = supabase.postgrest["results"]
                .insert(
                    mapOf(
                        "user_id"                  to userId,
                        "workout_id"               to result.workoutId,
                        "score"                    to result.score,
                        "score_numeric"            to result.scoreNumeric,
                        "rxd"                      to result.rxd,
                        "notes"                    to result.notes,
                        "rpe"                      to result.rpe,
                        "session_duration_minutes" to result.sessionDurationMinutes,
                        "is_official_submission"   to result.isOfficialSubmission,
                        "completed_at"             to now.toString()
                    )
                ) { select() }
                .decodeSingle<ResultRow>()

            // Query PRs written by the server trigger in the last 60 seconds.
            // The trigger runs synchronously with the insert so by the time
            // we receive the ResultRow the PR rows already exist.
            val newPrRows = supabase.postgrest["personal_records"]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("achieved_at", now.minusSeconds(60).toString())
                    }
                }
                .decodeList<PersonalRecordRow>()

            val newPrs = newPrRows.map { pr ->
                val mvRow = runCatching {
                    supabase.postgrest["movements"]
                        .select { filter { eq("id", pr.movement_id) } }
                        .decodeSingle<MovementRow>()
                }.getOrNull()
                pr.toDomain(mvRow?.name ?: pr.movement_name ?: pr.movement_id, mvRow?.category ?: "")
            }

            Result.success(resultRow.toDomain(newPrs))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getHistory(userId: String): Flow<List<WorkoutResult>> = flow {
        val rows = supabase.postgrest["results"]
            .select {
                filter { eq("user_id", userId) }
                order("completed_at", Order.DESCENDING)
                limit(100)
            }
            .decodeList<ResultRow>()
        emit(rows.map { it.toDomain() })
    }

    override fun getTodayWorkout(): Flow<Workout?> = flow {
        val today = LocalDate.now(ZoneOffset.UTC)
        val todayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant().toString()
        val todayEnd = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString()

        val rows = supabase.postgrest["workouts"]
            .select {
                filter {
                    gte("created_at", todayStart)
                    lt("created_at", todayEnd)
                }
                limit(1)
            }
            .decodeList<WorkoutRow>()

        val workout = rows.firstOrNull()?.let { row ->
            val movements = fetchMovementsForWorkout(row.id)
            row.toWorkout(movements)
        }
        emit(workout)
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private suspend fun fetchMovementsForWorkout(wodId: String): List<WorkoutMovement> {
        val wmRows = supabase.postgrest["workout_movements"]
            .select { filter { eq("workout_id", wodId) } }
            .decodeList<WorkoutMovementRow>()

        return wmRows.map { wmRow ->
            val mvRow = supabase.postgrest["movements"]
                .select { filter { eq("id", wmRow.movement_id) } }
                .decodeSingle<MovementRow>()
            wmRow.toDomain(mvRow)
        }.sortedBy { it.sortOrder }
    }

    private fun WorkoutRow.toSummary() = WorkoutSummary(
        id          = id,
        name        = name,
        timeDomain  = TimeDomain.valueOf(time_domain),
        movementCount = 0   // not fetched for list view — loaded lazily on detail
    )

    private fun WorkoutRow.toWorkout(movements: List<WorkoutMovement>) = Workout(
        id            = id,
        name          = name,
        description   = description ?: "",
        timeDomain    = TimeDomain.valueOf(time_domain),
        scoringMetric = ScoringMetric.valueOf(scoring_metric),
        timeCap       = time_cap_seconds?.let { Duration.ofSeconds(it.toLong()) },
        rounds        = rounds,
        movements     = movements
    )

    private fun WorkoutMovementRow.toDomain(movement: MovementRow) = WorkoutMovement(
        id                  = id,
        movement            = movement.toDomain(),
        prescribedReps      = prescribed_reps,
        prescribedWeight    = prescribed_weight_kg,
        prescribedDistance  = prescribed_distance_m,
        prescribedCalories  = prescribed_calories,
        sortOrder           = sort_order,
        repScheme           = rep_scheme
    )

    private fun MovementRow.toDomain() = Movement(
        id             = id,
        name           = name,
        category       = category,
        primaryMuscles = primary_muscles,
        equipment      = equipment
    )

    private fun ResultRow.toDomain(newPrs: List<PersonalRecord> = emptyList()) = WorkoutResult(
        id                   = id,
        workoutId            = workout_id,
        userId               = user_id,
        score                = score,
        rxd                  = rxd,
        notes                = notes,
        rpe                  = rpe,
        completedAt          = Instant.parse(completed_at),
        newPrs               = newPrs,
        coachNote            = coach_note,
        isOfficialSubmission = is_official_submission
    )

    private fun PersonalRecordRow.toDomain(movementName: String, category: String) = PersonalRecord(
        id           = id,
        userId       = user_id,
        movementId   = movement_id,
        movementName = movementName,
        category     = category,
        value        = value,
        unit         = PrUnit.valueOf(unit),
        achievedAt   = Instant.parse(achieved_at)
    )
}
