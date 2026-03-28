package com.apexai.crossfit.feature.readiness.domain.usecase

import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.feature.readiness.data.HealthConnectDataSource
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class SyncHealthDataUseCase @Inject constructor(
    private val healthConnectDataSource: HealthConnectDataSource,
    private val readinessRepository: ReadinessRepository,
    private val supabase: SupabaseClient
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")

        val now   = Instant.now()
        val since = now.minus(7, ChronoUnit.DAYS)

        val hrv    = healthConnectDataSource.readHrvData(since, now)
        val sleep  = healthConnectDataSource.readSleepData(since, now)
        val hr     = healthConnectDataSource.readHeartRateData(since, now)
        val restHr = healthConnectDataSource.readRestingHeartRate(since, now)

        val latestHrv   = hrv.maxByOrNull { it.timestamp }?.value
        val latestSleep = sleep.maxByOrNull { it.startTime }
        val totalSleepMin = latestSleep?.totalDuration?.toMinutes()?.toInt()
        val deepSleepMin  = latestSleep?.deepSleepDuration?.toMinutes()?.toInt()
        val remSleepMin   = latestSleep?.remSleepDuration?.toMinutes()?.toInt()

        val snapshot = HealthSnapshot(
            userId               = userId,
            hrvRmssd             = latestHrv,
            sleepDurationMinutes = totalSleepMin,
            deepSleepMinutes     = deepSleepMin,
            remSleepMinutes      = remSleepMin,
            restingHr            = restHr,
            capturedAt           = now
        )

        readinessRepository.syncHealthSnapshot(snapshot).getOrThrow()
    }
}
