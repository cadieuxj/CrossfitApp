package com.apexai.crossfit.core.domain.model

import android.graphics.PointF
import java.time.Duration
import java.time.Instant

// ============================================================
// Workout domain
// ============================================================

enum class TimeDomain { AMRAP, EMOM, RFT, TABATA, FOR_TIME, MAX_WEIGHT, CALORIES }
enum class ScoringMetric { REPS, TIME, LOAD, ROUNDS_PLUS_REPS }

data class Workout(
    val id: String,
    val name: String,
    val description: String,
    val timeDomain: TimeDomain,
    val scoringMetric: ScoringMetric,
    val timeCap: Duration?,
    val rounds: Int?,
    val movements: List<WorkoutMovement> = emptyList()
)

data class WorkoutSummary(
    val id: String,
    val name: String,
    val timeDomain: TimeDomain,
    val movementCount: Int
)

data class WorkoutMovement(
    val id: String,
    val movement: Movement,
    val prescribedReps: Int?,
    val prescribedWeight: Double?,
    val prescribedDistance: Double?,
    val prescribedCalories: Int?,
    val sortOrder: Int
)

data class Movement(
    val id: String,
    val name: String,
    val category: String,
    val primaryMuscles: List<String>,
    val equipment: String?
)

data class WorkoutResult(
    val id: String,
    val workoutId: String,
    val userId: String,
    val score: String,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?,
    val completedAt: Instant,
    val newPrs: List<PersonalRecord> = emptyList()
)

data class WorkoutResultInput(
    val workoutId: String,
    val score: String,
    val scoreNumeric: Double?,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?,
    val sessionDurationMinutes: Int? = null
)

// ============================================================
// Personal Records domain
// ============================================================

enum class PrUnit { KG, LBS, REPS, SECONDS }

data class PersonalRecord(
    val id: String,
    val userId: String,
    val movementId: String,
    val movementName: String,
    val category: String,
    val value: Double,
    val unit: PrUnit,
    val achievedAt: Instant
)

data class PrHistoryEntry(
    val value: Double,
    val unit: PrUnit,
    val achievedAt: Instant
)

// ============================================================
// Readiness domain
// ============================================================

enum class ReadinessZone { OPTIMAL, CAUTION, HIGH_RISK, UNDERTRAINED, ONBOARDING }
enum class SleepQuality   { EXCELLENT, GOOD, FAIR, POOR }

data class ReadinessScore(
    val acwr: Float,
    val zone: ReadinessZone,
    val acuteLoad: Float,
    val chronicLoad: Float,
    val hrvComponent: Int?,
    val sleepDurationMinutes: Int?,
    val restingHr: Int?,
    val calculatedAt: Instant,
    val recommendation: String
)

data class HrvReading(
    val value: Int,
    val timestamp: Instant
)

data class SleepSession(
    val startTime: Instant,
    val endTime: Instant,
    val totalDuration: Duration,
    val deepSleepDuration: Duration,
    val remSleepDuration: Duration,
    val lightSleepDuration: Duration
)

data class HeartRateReading(
    val bpm: Int,
    val timestamp: Instant
)

data class HealthSnapshot(
    val userId: String,
    val hrvRmssd: Int?,
    val sleepDurationMinutes: Int?,
    val deepSleepMinutes: Int?,
    val remSleepMinutes: Int?,
    val restingHr: Int?,
    val capturedAt: Instant,
    val sorenessScore: Int? = null,
    val perceivedReadiness: Int? = null,
    val moodScore: Int? = null
)

// ============================================================
// Coaching / Vision domain
// ============================================================

enum class FaultSeverity { MINOR, MODERATE, CRITICAL }

data class CoachingReport(
    val id: String,
    val videoId: String,
    val movementType: String,
    val overallAssessment: String,
    val repCount: Int,
    val estimatedWeight: Double?,
    val faults: List<MovementFault>,
    val globalCues: List<String>,
    val createdAt: Instant,
    val clipDurationMs: Long? = null
)

data class MovementFault(
    val id: String,
    val description: String,
    val severity: FaultSeverity,
    val timestampMs: Long,
    val cue: String,
    val correctedImageUrl: String?,
    val affectedJoints: List<String>
)

data class PoseOverlayData(
    val landmarks: List<PoseLandmark>,
    val jointAngles: Map<JointAngle, Float>,
    val barbellPosition: PointF?,
    val barbellTrajectory: List<PointF>,
    val frameTimestamp: Long,
    val isDepthEnriched: Boolean = false
)

data class PoseLandmark(
    val index: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float
)

enum class JointAngle {
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_HIP, RIGHT_HIP,
    LEFT_ELBOW, RIGHT_ELBOW,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ANKLE, RIGHT_ANKLE,
    TRUNK_INCLINATION
}

data class TimedPoseOverlay(
    val timestampMs: Long,
    val landmarks: List<PoseLandmark>,
    val jointAngles: Map<JointAngle, Float>
)

data class FaultMarker(
    val timestampMs: Long,
    val label: String,
    val severity: FaultSeverity
)

// ============================================================
// Auth domain
// ============================================================

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val expiresAt: Long
)

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Instant,
    val avatarUrl: String?
)
