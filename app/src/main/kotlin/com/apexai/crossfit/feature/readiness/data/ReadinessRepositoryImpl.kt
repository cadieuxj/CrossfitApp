package com.apexai.crossfit.feature.readiness.data

import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class HealthSnapshotRow(
    val hrv_rmssd: Int? = null,
    val sleep_duration_minutes: Int? = null,
    val resting_hr: Int? = null,
    val captured_at: String,
    val acwr_ratio: Float? = null,
    val soreness_score: Int? = null,
    val perceived_readiness: Int? = null,
    val mood_score: Int? = null
)

private fun computeZoneFromAcwr(acwr: Double): ReadinessZone = when {
    acwr <= 0.0   -> ReadinessZone.ONBOARDING
    acwr < 0.8    -> ReadinessZone.UNDERTRAINED
    acwr <= 1.3   -> ReadinessZone.OPTIMAL
    acwr <= 1.5   -> ReadinessZone.CAUTION
    else          -> ReadinessZone.HIGH_RISK
}

@Serializable
data class ReadinessRpcResponse(
    val acwr: Float,
    val zone: String,
    val acute_load: Float,
    val chronic_load: Float,
    val hrv_rmssd: Int? = null,
    val sleep_duration_minutes: Int? = null,
    val resting_hr: Int? = null,
    val recommendation: String
)

@Singleton
class ReadinessRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val healthConnectDataSource: HealthConnectDataSource
) : ReadinessRepository {

    override fun getReadinessScore(userId: String): Flow<ReadinessScore> = flow {
        val result = supabase.postgrest.rpc(
            function = "calculate_readiness",
            parameters = mapOf("p_user_id" to userId)
        ).decodeAs<ReadinessRpcResponse>()

        emit(
            ReadinessScore(
                acwr                  = result.acwr,
                zone                  = ReadinessZone.valueOf(result.zone),
                acuteLoad             = result.acute_load,
                chronicLoad           = result.chronic_load,
                hrvComponent          = result.hrv_rmssd,
                sleepDurationMinutes  = result.sleep_duration_minutes,
                restingHr             = result.resting_hr,
                calculatedAt          = Instant.now(),
                recommendation        = result.recommendation
            )
        )
    }

    override suspend fun syncHealthSnapshot(snapshot: HealthSnapshot): Result<Unit> = runCatching {
        supabase.postgrest["health_snapshots"]
            .upsert(
                mapOf(
                    "user_id"                to snapshot.userId,
                    "hrv_rmssd"              to snapshot.hrvRmssd,
                    "sleep_duration_minutes" to snapshot.sleepDurationMinutes,
                    "deep_sleep_minutes"     to snapshot.deepSleepMinutes,
                    "rem_sleep_minutes"      to snapshot.remSleepMinutes,
                    "resting_hr"             to snapshot.restingHr,
                    "captured_at"            to snapshot.capturedAt.toString(),
                    "soreness_score"         to snapshot.sorenessScore,
                    "perceived_readiness"    to snapshot.perceivedReadiness,
                    "mood_score"             to snapshot.moodScore
                )
            ) {
                onConflict = "user_id,captured_at"
            }
        Unit
    }

    override fun getReadinessHistory(userId: String, days: Int): Flow<List<ReadinessScore>> = flow {
        val since = Instant.now().minus(days.toLong(), ChronoUnit.DAYS).toString()
        val rows = supabase.postgrest["health_snapshots"]
            .select {
                filter {
                    eq("user_id", userId)
                    gte("captured_at", since)
                }
                order("captured_at", ascending = false)
            }
            .decodeList<HealthSnapshotRow>()

        val scores = rows.map { row ->
            val acwr = row.acwr_ratio?.toDouble() ?: 0.0
            ReadinessScore(
                acwr                 = acwr.toFloat(),
                zone                 = computeZoneFromAcwr(acwr),
                acuteLoad            = 0f,
                chronicLoad          = 0f,
                hrvComponent         = row.hrv_rmssd,
                sleepDurationMinutes = row.sleep_duration_minutes,
                restingHr            = row.resting_hr,
                calculatedAt         = Instant.parse(row.captured_at),
                recommendation       = ""
            )
        }
        emit(scores)
    }

    override suspend fun checkHealthConnectPermissions(): Boolean =
        healthConnectDataSource.checkPermissions()
}
