package com.apexai.crossfit.feature.readiness

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord.Stage
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.apexai.crossfit.feature.readiness.data.HealthConnectDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for Health Connect permission flow and data reading.
 *
 * These tests verify:
 * 1. [HealthConnectDataSource.checkPermissions] returns false when permissions absent.
 * 2. HRV data is correctly mapped from [HeartRateVariabilityRmssdRecord] to [HrvReading].
 * 3. Sleep data is correctly mapped from [SleepSessionRecord] to [SleepSession],
 *    including stage breakdown (deep/REM/light).
 * 4. ACWR zone classification is correct for the mapped data.
 *
 * Tests that require actual Health Connect installation are guarded with
 * [assumeTrue] and skip gracefully on emulators without Health Connect.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class HealthConnectIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // --------------------------------------------------------
    // Health Connect availability
    // --------------------------------------------------------

    @Test
    fun healthConnect_sdkStatusChecked_doesNotCrash() = runTest {
        // Just verify the SDK status check call completes without throwing.
        // Health Connect may not be installed on all test emulators.
        val status = HealthConnectClient.getSdkStatus(context)
        assertTrue(
            "SDK status must be one of the valid constants",
            status in listOf(
                HealthConnectClient.SDK_AVAILABLE,
                HealthConnectClient.SDK_UNAVAILABLE,
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
            )
        )
    }

    @Test
    fun checkPermissions_healthConnectAvailable_returnsBoolean() = runTest {
        val sdkAvailable = HealthConnectClient.getSdkStatus(context) ==
                HealthConnectClient.SDK_AVAILABLE
        assumeTrue("Health Connect SDK not available on this device", sdkAvailable)

        val dataSource = HealthConnectDataSource(context)
        // In a fresh test environment permissions are not pre-granted
        val result = dataSource.checkPermissions()

        // Just assert the result is a valid boolean — not a crash
        assertFalse("Fresh test environment should not have permissions pre-granted", result)
    }

    // --------------------------------------------------------
    // HRV data mapping — tested with mock HealthConnectClient
    // --------------------------------------------------------

    @Test
    fun readHrvData_mockClient_mapsToHrvReadingCorrectly() = runTest {
        val start = Instant.parse("2026-03-27T22:00:00Z")
        val end   = Instant.parse("2026-03-28T08:00:00Z")

        val mockRecord = mockk<HeartRateVariabilityRmssdRecord> {
            every { heartRateVariabilityMillis } returns 65.0
            every { time } returns Instant.parse("2026-03-28T06:00:00Z")
        }

        val mockResponse = mockk<androidx.health.connect.client.response.ReadRecordsResponse<HeartRateVariabilityRmssdRecord>> {
            every { records } returns listOf(mockRecord)
        }

        val mockClient = mockk<HealthConnectClient> {
            coEvery {
                readRecords(
                    match<androidx.health.connect.client.request.ReadRecordsRequest<HeartRateVariabilityRmssdRecord>> { true }
                )
            } returns mockResponse
        }

        val dataSource = spyk(HealthConnectDataSource(context)) {
            every { this@spyk["client"] as HealthConnectClient } returns mockClient
        }

        // Verify the mapping result
        val readings = dataSource.readHrvData(start, end)

        assertEquals(1, readings.size)
        assertEquals(65, readings[0].value)
        assertEquals(Instant.parse("2026-03-28T06:00:00Z"), readings[0].timestamp)
    }

    // --------------------------------------------------------
    // Sleep data mapping
    // --------------------------------------------------------

    @Test
    fun readSleepData_withStages_mapsDeepRemLightCorrectly() = runTest {
        val sleepStart = Instant.parse("2026-03-27T22:00:00Z")
        val sleepEnd   = Instant.parse("2026-03-28T06:30:00Z")

        val deepStart  = Instant.parse("2026-03-28T00:00:00Z")
        val deepEnd    = Instant.parse("2026-03-28T01:30:00Z") // 90 min deep

        val remStart   = Instant.parse("2026-03-28T03:00:00Z")
        val remEnd     = Instant.parse("2026-03-28T04:00:00Z") // 60 min REM

        val lightStart = Instant.parse("2026-03-28T04:00:00Z")
        val lightEnd   = Instant.parse("2026-03-28T06:00:00Z") // 120 min light

        val deepStage = mockk<Stage> {
            every { stage }     returns SleepSessionRecord.STAGE_TYPE_DEEP
            every { startTime } returns deepStart
            every { endTime }   returns deepEnd
        }
        val remStage = mockk<Stage> {
            every { stage }     returns SleepSessionRecord.STAGE_TYPE_REM
            every { startTime } returns remStart
            every { endTime }   returns remEnd
        }
        val lightStage = mockk<Stage> {
            every { stage }     returns SleepSessionRecord.STAGE_TYPE_LIGHT
            every { startTime } returns lightStart
            every { endTime }   returns lightEnd
        }

        val mockRecord = mockk<SleepSessionRecord> {
            every { startTime } returns sleepStart
            every { endTime }   returns sleepEnd
            every { stages }    returns listOf(deepStage, remStage, lightStage)
        }

        val mockResponse = mockk<androidx.health.connect.client.response.ReadRecordsResponse<SleepSessionRecord>> {
            every { records } returns listOf(mockRecord)
        }

        val mockClient = mockk<HealthConnectClient> {
            coEvery {
                readRecords(
                    match<androidx.health.connect.client.request.ReadRecordsRequest<SleepSessionRecord>> { true }
                )
            } returns mockResponse
        }

        val dataSource = spyk(HealthConnectDataSource(context)) {
            every { this@spyk["client"] as HealthConnectClient } returns mockClient
        }

        val sessions = dataSource.readSleepData(sleepStart, sleepEnd)

        assertEquals(1, sessions.size)
        val session = sessions[0]
        assertEquals(90L, session.deepSleepDuration.toMinutes())
        assertEquals(60L, session.remSleepDuration.toMinutes())
        assertEquals(120L, session.lightSleepDuration.toMinutes())
        assertEquals(sleepStart, session.startTime)
        assertEquals(sleepEnd, session.endTime)
    }

    // --------------------------------------------------------
    // ACWR calculation using mapped Health Connect data
    // --------------------------------------------------------

    @Test
    fun acwrCalculation_withRealLoadValues_producesCorrectZone() {
        // Simulate a week where athlete trained at a sustainable 10% ramp rate
        val acuteLoad   = 550f   // 7-day training load
        val chronicLoad = 500f   // 28-day rolling average

        val acwr = AcwrZoneClassifier.calculate(acuteLoad, chronicLoad)
        val zone = AcwrZoneClassifier.classify(acwr)

        assertEquals(1.1f, acwr, 0.01f)
        assertEquals(ReadinessZone.OPTIMAL, zone)
    }

    @Test
    fun acwrCalculation_highAcuteLoad_producesHighRiskZone() {
        val acuteLoad   = 800f
        val chronicLoad = 500f

        val acwr = AcwrZoneClassifier.calculate(acuteLoad, chronicLoad)
        val zone = AcwrZoneClassifier.classify(acwr)

        assertEquals(1.6f, acwr, 0.01f)
        assertEquals(ReadinessZone.HIGH_RISK, zone)
    }
}
