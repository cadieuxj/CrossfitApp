package com.apexai.crossfit.feature.pr.data

import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.core.domain.model.PrUnit
import com.apexai.crossfit.feature.pr.domain.PrRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PrWithMovementRow(
    val id: String,
    val user_id: String,
    val movement_id: String,
    val value: Double,
    val unit: String,
    val achieved_at: String,
    val movements: MovementNameRow
)

@Serializable
data class MovementNameRow(
    val name: String,
    val category: String
)

@Singleton
class PrRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : PrRepository {

    override fun getAllPrs(userId: String): Flow<Map<String, List<PersonalRecord>>> = flow {
        val rows = supabase.postgrest["personal_records"]
            .select {
                filter { eq("user_id", userId) }
                order("achieved_at", Order.DESCENDING)
            }
            .decodeList<PrWithMovementRow>()

        val mapped = rows.map { row ->
            PersonalRecord(
                id           = row.id,
                userId       = row.user_id,
                movementId   = row.movement_id,
                movementName = row.movements.name,
                category     = row.movements.category,
                value        = row.value,
                unit         = PrUnit.valueOf(row.unit),
                achievedAt   = Instant.parse(row.achieved_at)
            )
        }
        emit(mapped.groupBy { it.category })
    }

    override fun getPrHistory(userId: String, movementId: String): Flow<List<PrHistoryEntry>> = flow {
        // Query results for this movement, sorted ascending to build trend
        val rows = supabase.postgrest["results"]
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("completed_at", Order.ASCENDING)
            }
            .decodeList<com.apexai.crossfit.feature.wod.data.ResultRow>()

        // Map to PrHistoryEntry (simplified — full implementation would join workout_movements)
        val history = rows.mapNotNull { row ->
            val v = row.score_numeric ?: return@mapNotNull null
            PrHistoryEntry(
                value      = v,
                unit       = PrUnit.KG,
                achievedAt = Instant.parse(row.completed_at)
            )
        }
        emit(history)
    }
}
