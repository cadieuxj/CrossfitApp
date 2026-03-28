package com.apexai.crossfit.feature.readiness.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.apexai.crossfit.core.domain.model.HeartRateReading
import com.apexai.crossfit.core.domain.model.HrvReading
import com.apexai.crossfit.core.domain.model.SleepSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun checkPermissions(): Boolean = runCatching {
        val required = setOf(
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        )
        val granted = client.permissionController.getGrantedPermissions()
        required.all { it in granted }
    }.getOrDefault(false)

    suspend fun readHrvData(start: Instant, end: Instant): List<HrvReading> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.map {
            HrvReading(
                value     = it.heartRateVariabilityMillis.toInt(),
                timestamp = it.time
            )
        }
    }

    suspend fun readSleepData(start: Instant, end: Instant): List<SleepSession> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.map { record ->
            val total = Duration.between(record.startTime, record.endTime)
            var deep  = Duration.ZERO
            var rem   = Duration.ZERO
            var light = Duration.ZERO
            record.stages.forEach { stage ->
                val d = Duration.between(stage.startTime, stage.endTime)
                when (stage.stage) {
                    SleepSessionRecord.STAGE_TYPE_DEEP  -> deep  += d
                    SleepSessionRecord.STAGE_TYPE_REM   -> rem   += d
                    SleepSessionRecord.STAGE_TYPE_LIGHT -> light += d
                    else -> {}
                }
            }
            SleepSession(
                startTime          = record.startTime,
                endTime            = record.endTime,
                totalDuration      = total,
                deepSleepDuration  = deep,
                remSleepDuration   = rem,
                lightSleepDuration = light
            )
        }
    }

    suspend fun readHeartRateData(start: Instant, end: Instant): List<HeartRateReading> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.flatMap { record ->
            record.samples.map {
                HeartRateReading(bpm = it.beatsPerMinute.toInt(), timestamp = it.time)
            }
        }
    }

    suspend fun readRestingHeartRate(start: Instant, end: Instant): Int? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = RestingHeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response.records.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
    }
}
