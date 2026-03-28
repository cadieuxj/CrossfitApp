package com.apexai.crossfit.feature.competition.data

import com.apexai.crossfit.core.domain.model.CompetitionEvent
import com.apexai.crossfit.core.domain.model.CompetitionStanding
import com.apexai.crossfit.core.domain.model.CompetitionStandingInput
import com.apexai.crossfit.core.domain.model.CompetitionStatus
import com.apexai.crossfit.core.domain.model.CompetitionType
import com.apexai.crossfit.feature.competition.domain.CompetitionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CompetitionEventRow(
    val id: String,
    val name: String,
    val competition_type: String,
    val status: String,
    val start_date: String,
    val end_date: String,
    val description: String? = null,
    val leaderboard_url: String? = null
)

@Serializable
data class CompetitionStandingRow(
    val id: String,
    val user_id: String,
    val event_id: String,
    val workout_name: String,
    val score: String,
    val score_numeric: Double? = null,
    val division: String = "RX",
    val rank_overall: Int? = null,
    val rank_age_group: Int? = null,
    val percentile: Double? = null,
    val submitted_at: String
)

@Singleton
class CompetitionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : CompetitionRepository {

    override fun getEvents(): Flow<List<CompetitionEvent>> = flow {
        val rows = supabase.postgrest["competition_events"]
            .select { order("start_date", Order.DESCENDING) }
            .decodeList<CompetitionEventRow>()
        emit(rows.map { it.toDomain() })
    }

    override fun getActiveEvents(): Flow<List<CompetitionEvent>> = flow {
        val rows = supabase.postgrest["competition_events"]
            .select {
                filter { eq("status", "ACTIVE") }
                order("start_date", Order.ASCENDING)
            }
            .decodeList<CompetitionEventRow>()
        emit(rows.map { it.toDomain() })
    }

    override fun getEventById(eventId: String): Flow<CompetitionEvent> = flow {
        val row = supabase.postgrest["competition_events"]
            .select { filter { eq("id", eventId) } }
            .decodeSingle<CompetitionEventRow>()
        emit(row.toDomain())
    }

    override fun getMyStandings(eventId: String): Flow<List<CompetitionStanding>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@flow
        val rows = supabase.postgrest["competition_standings"]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("event_id", eventId)
                }
                order("submitted_at", Order.DESCENDING)
            }
            .decodeList<CompetitionStandingRow>()
        emit(rows.map { it.toDomain() })
    }

    override suspend fun upsertStanding(input: CompetitionStandingInput): Result<CompetitionStanding> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not authenticated"))

            val row = supabase.postgrest["competition_standings"]
                .upsert(
                    mapOf(
                        "user_id"       to userId,
                        "event_id"      to input.eventId,
                        "workout_name"  to input.workoutName,
                        "score"         to input.score,
                        "score_numeric" to input.scoreNumeric,
                        "division"      to input.division,
                        "rank_overall"  to input.rankOverall,
                        "rank_age_group" to input.rankAgeGroup,
                        "percentile"    to input.percentile
                    ),
                    onConflict = "user_id,event_id,workout_name"
                ) { select() }
                .decodeSingle<CompetitionStandingRow>()

            Result.success(row.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStanding(standingId: String): Result<Unit> {
        return try {
            supabase.postgrest["competition_standings"]
                .delete { filter { eq("id", standingId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CompetitionEventRow.toDomain() = CompetitionEvent(
        id              = id,
        name            = name,
        type            = CompetitionType.valueOf(competition_type),
        status          = CompetitionStatus.valueOf(status),
        startDate       = LocalDate.parse(start_date),
        endDate         = LocalDate.parse(end_date),
        description     = description,
        leaderboardUrl  = leaderboard_url
    )

    private fun CompetitionStandingRow.toDomain() = CompetitionStanding(
        id           = id,
        userId       = user_id,
        eventId      = event_id,
        workoutName  = workout_name,
        score        = score,
        scoreNumeric = score_numeric,
        division     = division,
        rankOverall  = rank_overall,
        rankAgeGroup = rank_age_group,
        percentile   = percentile,
        submittedAt  = Instant.parse(submitted_at)
    )
}
