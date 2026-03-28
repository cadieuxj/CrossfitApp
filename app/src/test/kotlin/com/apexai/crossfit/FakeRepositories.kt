package com.apexai.crossfit

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.core.domain.model.PrUnit
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.UserProfile
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import com.apexai.crossfit.feature.pr.domain.PrRepository
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import com.apexai.crossfit.feature.wod.domain.WodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant

// ============================================================
// Test fixture builders — centralised so every test file gets
// the same valid domain objects without copy-pasting.
// ============================================================

object Fixtures {

    fun workout(
        id: String = "wod-1",
        name: String = "Fran",
        timeDomain: TimeDomain = TimeDomain.RFT,
        scoringMetric: ScoringMetric = ScoringMetric.TIME,
        rounds: Int? = null,
        timeCap: Duration? = Duration.ofMinutes(20)
    ) = Workout(
        id            = id,
        name          = name,
        description   = "21-15-9 Thrusters and Pull-ups",
        timeDomain    = timeDomain,
        scoringMetric = scoringMetric,
        timeCap       = timeCap,
        rounds        = rounds,
        movements     = emptyList()
    )

    fun workoutResult(
        id: String = "result-1",
        workoutId: String = "wod-1",
        score: String = "3:45",
        newPrs: List<PersonalRecord> = emptyList()
    ) = WorkoutResult(
        id          = id,
        workoutId   = workoutId,
        userId      = "user-1",
        score       = score,
        rxd         = true,
        notes       = null,
        rpe         = 8,
        completedAt = Instant.parse("2026-03-28T09:00:00Z"),
        newPrs      = newPrs
    )

    fun personalRecord(
        id: String = "pr-1",
        movementId: String = "mov-1",
        movementName: String = "Thruster",
        category: String = "Olympic Lifting",
        value: Double = 60.0,
        unit: PrUnit = PrUnit.KG
    ) = PersonalRecord(
        id           = id,
        userId       = "user-1",
        movementId   = movementId,
        movementName = movementName,
        category     = category,
        value        = value,
        unit         = unit,
        achievedAt   = Instant.parse("2026-03-28T09:00:00Z")
    )

    fun readinessScore(
        acwr: Float = 1.1f,
        zone: ReadinessZone = ReadinessZone.OPTIMAL,
        acuteLoad: Float = 550f,
        chronicLoad: Float = 500f,
        hrv: Int? = 65,
        sleepMinutes: Int? = 480,
        restingHr: Int? = 52,
        recommendation: String = "Good to train at full intensity."
    ) = ReadinessScore(
        acwr                 = acwr,
        zone                 = zone,
        acuteLoad            = acuteLoad,
        chronicLoad          = chronicLoad,
        hrvComponent         = hrv,
        sleepDurationMinutes = sleepMinutes,
        restingHr            = restingHr,
        calculatedAt         = Instant.parse("2026-03-28T06:00:00Z"),
        recommendation       = recommendation
    )

    fun authSession() = AuthSession(
        accessToken  = "access-token-123",
        refreshToken = "refresh-token-456",
        userId       = "user-1",
        expiresAt    = Instant.now().plusSeconds(3600).toEpochMilli()
    )

    fun userProfile() = UserProfile(
        id          = "user-1",
        email       = "athlete@example.com",
        displayName = "Test Athlete",
        createdAt   = Instant.parse("2026-01-01T00:00:00Z"),
        avatarUrl   = null
    )
}

// ============================================================
// FakeWodRepository
// ============================================================

class FakeWodRepository : WodRepository {

    /** Programmatically control what getWorkoutById emits. */
    var workoutToReturn: Workout = Fixtures.workout()

    /** Programmatically control what logResult returns. */
    var logResultResponse: Result<WorkoutResult> =
        Result.success(Fixtures.workoutResult())

    /** Allows tests to assert how many times logResult was called. */
    var logResultCallCount: Int = 0

    /** Last input passed to logResult — inspect in assertions. */
    var lastLogResultInput: WorkoutResultInput? = null

    /** Control whether getWorkoutById throws. */
    var getWorkoutError: Throwable? = null

    override fun getWorkouts(
        query: String?,
        timeDomain: TimeDomain?
    ): Flow<List<WorkoutSummary>> = flow {
        emit(listOf(WorkoutSummary(workoutToReturn.id, workoutToReturn.name, workoutToReturn.timeDomain, 2)))
    }

    override fun getWorkoutById(wodId: String): Flow<Workout> = flow {
        getWorkoutError?.let { throw it }
        emit(workoutToReturn)
    }

    override fun getWorkoutMovements(wodId: String): Flow<List<WorkoutMovement>> = flow {
        emit(emptyList())
    }

    override suspend fun logResult(result: WorkoutResultInput): Result<WorkoutResult> {
        logResultCallCount++
        lastLogResultInput = result
        return logResultResponse
    }

    override fun getHistory(userId: String): Flow<List<WorkoutResult>> = flow {
        emit(emptyList())
    }

    override fun getTodayWorkout(): Flow<Workout?> = flow {
        emit(workoutToReturn)
    }
}

// ============================================================
// FakePrRepository
// ============================================================

class FakePrRepository : PrRepository {

    var prsToReturn: Map<String, List<PersonalRecord>> = emptyMap()
    var prHistoryToReturn: List<PrHistoryEntry> = emptyList()

    /** Replace the backing state flow so tests can push new data mid-test. */
    private val prsFlow = MutableSharedFlow<Map<String, List<PersonalRecord>>>(replay = 1)
    private val historyFlow = MutableSharedFlow<List<PrHistoryEntry>>(replay = 1)

    suspend fun emitPrs(prs: Map<String, List<PersonalRecord>>) = prsFlow.emit(prs)
    suspend fun emitHistory(history: List<PrHistoryEntry>) = historyFlow.emit(history)

    override fun getAllPrs(userId: String): Flow<Map<String, List<PersonalRecord>>> = flow {
        emit(prsToReturn)
    }

    override fun getPrHistory(
        userId: String,
        movementId: String
    ): Flow<List<PrHistoryEntry>> = flow {
        emit(prHistoryToReturn)
    }
}

// ============================================================
// FakeReadinessRepository
// ============================================================

class FakeReadinessRepository : ReadinessRepository {

    var readinessScoreToReturn: ReadinessScore = Fixtures.readinessScore()
    var checkPermissionsResult: Boolean = true
    var syncResult: Result<Unit> = Result.success(Unit)
    var readinessError: Throwable? = null

    private val readinessFlow = MutableStateFlow(readinessScoreToReturn)

    suspend fun emitScore(score: ReadinessScore) = readinessFlow.emit(score)

    override fun getReadinessScore(userId: String): Flow<ReadinessScore> = flow {
        readinessError?.let { throw it }
        emit(readinessScoreToReturn)
    }

    override fun getReadinessHistory(userId: String, days: Int): Flow<List<ReadinessScore>> = flow {
        emit(listOf(readinessScoreToReturn))
    }

    override suspend fun syncHealthSnapshot(snapshot: HealthSnapshot): Result<Unit> = syncResult

    override suspend fun checkHealthConnectPermissions(): Boolean = checkPermissionsResult
}

// ============================================================
// FakeAuthRepository
// ============================================================

class FakeAuthRepository : AuthRepository {

    var loginResult: Result<AuthSession> = Result.success(Fixtures.authSession())
    var registerResult: Result<AuthSession> = Result.success(Fixtures.authSession())
    var logoutResult: Result<Unit> = Result.success(Unit)
    var currentProfile: Result<UserProfile> = Result.success(Fixtures.userProfile())
    var sessionFlow = MutableStateFlow<AuthSession?>(Fixtures.authSession())

    override suspend fun login(email: String, password: String): Result<AuthSession> = loginResult

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthSession> = registerResult

    override suspend fun logout(): Result<Unit> = logoutResult

    override fun observeSession(): Flow<AuthSession?> = sessionFlow

    override suspend fun refreshToken(): Result<AuthSession> = loginResult

    override suspend fun getCurrentProfile(): Result<UserProfile> = currentProfile
}
