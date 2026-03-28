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
    val sortOrder: Int,
    val repScheme: String? = null   // e.g. "21-15-9", "50-40-30-20-10"; null for fixed-rep movements
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
    val newPrs: List<PersonalRecord> = emptyList(),
    val coachNote: String? = null,
    val isOfficialSubmission: Boolean = false
)

data class WorkoutResultInput(
    val workoutId: String,
    val score: String,
    val scoreNumeric: Double?,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?,
    val sessionDurationMinutes: Int? = null,
    val isOfficialSubmission: Boolean = false
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
// Competition domain
// ============================================================

enum class CompetitionType { OPEN, QUARTERFINALS, SEMIFINALS, GAMES, LOCAL, VIRTUAL }
enum class CompetitionStatus { UPCOMING, ACTIVE, COMPLETED }

data class CompetitionEvent(
    val id: String,
    val name: String,
    val type: CompetitionType,
    val status: CompetitionStatus,
    val startDate: java.time.LocalDate,
    val endDate: java.time.LocalDate,
    val description: String?,
    val leaderboardUrl: String?
)

data class CompetitionStanding(
    val id: String,
    val userId: String,
    val eventId: String,
    val workoutName: String,
    val score: String,
    val scoreNumeric: Double?,
    val division: String,
    val rankOverall: Int?,
    val rankAgeGroup: Int?,
    val percentile: Double?,
    val submittedAt: Instant
)

data class CompetitionStandingInput(
    val eventId: String,
    val workoutName: String,
    val score: String,
    val scoreNumeric: Double?,
    val division: String = "RX",
    val rankOverall: Int? = null,
    val rankAgeGroup: Int? = null,
    val percentile: Double? = null
)

// ============================================================
// Nutrition domain
// ============================================================

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK, PRE_WORKOUT, POST_WORKOUT }

data class MacroTargets(
    val userId: String,
    val caloriesKcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val restDayCalories: Int? = null,
    val restDayProteinG: Int? = null,
    val restDayCarbsG: Int? = null,
    val restDayFatG: Int? = null
)

data class MacroEntry(
    val id: String,
    val userId: String,
    val loggedDate: java.time.LocalDate,
    val mealType: MealType,
    val foodName: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val notes: String?,
    val loggedAt: Instant
)

data class MacroEntryInput(
    val loggedDate: java.time.LocalDate,
    val mealType: MealType,
    val foodName: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val notes: String? = null
)

data class DailyMacroSummary(
    val date: java.time.LocalDate,
    val totalCalories: Int,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val entries: List<MacroEntry>
)

data class CommonFood(
    val id: Int,
    val name: String,
    val servingG: Int,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val category: String?
)

// ============================================================
// Coach integration domain
// ============================================================

enum class CoachConnectionStatus { LINKED, UNLINKED }

data class CoachInfo(
    val id: String,
    val displayName: String,
    val gymName: String?,
    val inviteCode: String
)

data class CoachConnection(
    val id: String,
    val coachId: String,
    val coachDisplayName: String,
    val gymName: String?,
    val permissions: List<String>,
    val connectedAt: Instant
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
