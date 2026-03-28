package com.apexai.crossfit.feature.coach.data

import com.apexai.crossfit.core.domain.model.CoachConnection
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.feature.coach.domain.AthleteInfo
import com.apexai.crossfit.feature.coach.domain.CoachRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CoachRow(
    val id: String,
    val display_name: String,
    val gym_name: String? = null,
    val invite_code: String
)

@Serializable
data class CoachConnectionRow(
    val id: String,
    val coach_id: String,
    val athlete_id: String,
    val permissions: List<String>,
    val connected_at: String
)

@Serializable
data class AthleteProfileRow(
    val id: String,
    val email: String? = null,
    val display_name: String? = null
)

@Serializable
data class CoachResultRow(
    val id: String,
    val user_id: String,
    val workout_id: String,
    val score: String,
    val rxd: Boolean,
    val notes: String? = null,
    val rpe: Int? = null,
    val completed_at: String,
    val coach_note: String? = null,
    val is_official_submission: Boolean = false
)

@Singleton
class CoachRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : CoachRepository {

    override fun getMyCoaches(): Flow<List<CoachConnection>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: run { emit(emptyList()); return@flow }

        val connectionRows = supabase.postgrest["coach_connections"]
            .select { filter { eq("athlete_id", userId) } }
            .decodeList<CoachConnectionRow>()

        val connections = connectionRows.map { conn ->
            val coach = runCatching {
                supabase.postgrest["coaches"]
                    .select { filter { eq("id", conn.coach_id) } }
                    .decodeSingle<CoachRow>()
            }.getOrNull()

            CoachConnection(
                id               = conn.id,
                coachId          = conn.coach_id,
                coachDisplayName = coach?.display_name ?: "Unknown Coach",
                gymName          = coach?.gym_name,
                permissions      = conn.permissions,
                connectedAt      = Instant.parse(conn.connected_at)
            )
        }
        emit(connections)
    }

    override suspend fun linkByCode(inviteCode: String): Result<CoachConnection> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not authenticated"))

            // Look up coach by invite code
            val coachRows = supabase.postgrest["coaches"]
                .select { filter { eq("invite_code", inviteCode.uppercase().trim()) } }
                .decodeList<CoachRow>()

            val coach = coachRows.firstOrNull()
                ?: return Result.failure(Exception("No coach found with code ${inviteCode.uppercase()}"))

            // Create connection
            val connRow = supabase.postgrest["coach_connections"]
                .upsert(
                    mapOf(
                        "coach_id"    to coach.id,
                        "athlete_id"  to userId,
                        "permissions" to listOf("view_results", "view_prs")
                    ),
                    onConflict = "coach_id,athlete_id"
                ) { select() }
                .decodeSingle<CoachConnectionRow>()

            Result.success(
                CoachConnection(
                    id               = connRow.id,
                    coachId          = coach.id,
                    coachDisplayName = coach.display_name,
                    gymName          = coach.gym_name,
                    permissions      = connRow.permissions,
                    connectedAt      = Instant.parse(connRow.connected_at)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkCoach(connectionId: String): Result<Unit> {
        return try {
            supabase.postgrest["coach_connections"]
                .delete { filter { eq("id", connectionId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMyAthletes(): Flow<List<AthleteInfo>> = flow {
        val coachId = supabase.auth.currentUserOrNull()?.id ?: run { emit(emptyList()); return@flow }

        val connections = supabase.postgrest["coach_connections"]
            .select { filter { eq("coach_id", coachId) } }
            .decodeList<CoachConnectionRow>()

        val athletes = connections.map { conn ->
            val profile = runCatching {
                supabase.postgrest["profiles"]
                    .select { filter { eq("id", conn.athlete_id) } }
                    .decodeSingle<AthleteProfileRow>()
            }.getOrNull()

            AthleteInfo(
                userId      = conn.athlete_id,
                displayName = profile?.display_name ?: "Athlete",
                email       = profile?.email
            )
        }
        emit(athletes)
    }

    override fun getAthleteResults(athleteId: String): Flow<List<WorkoutResult>> = flow {
        val rows = supabase.postgrest["results"]
            .select {
                filter { eq("user_id", athleteId) }
                order("completed_at", Order.DESCENDING)
                limit(20)
            }
            .decodeList<CoachResultRow>()

        emit(rows.map { it.toDomain() })
    }

    override suspend fun addCoachNote(resultId: String, note: String): Result<Unit> {
        return try {
            supabase.postgrest["results"]
                .update(mapOf("coach_note" to note)) {
                    filter { eq("id", resultId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CoachResultRow.toDomain() = WorkoutResult(
        id                   = id,
        workoutId            = workout_id,
        userId               = user_id,
        score                = score,
        rxd                  = rxd,
        notes                = notes,
        rpe                  = rpe,
        completedAt          = Instant.parse(completed_at),
        coachNote            = coach_note,
        isOfficialSubmission = is_official_submission
    )
}
