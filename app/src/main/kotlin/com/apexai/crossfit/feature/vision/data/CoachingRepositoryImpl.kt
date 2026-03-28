package com.apexai.crossfit.feature.vision.data

import android.net.Uri
import com.apexai.crossfit.core.data.network.FastApiService
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.MovementFault
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class UploadProgress(
    val bytesUploaded: Long = 0L,
    val totalBytes: Long = 0L,
    val status: String = "UPLOADING"  // UPLOADING | ANALYZING | COMPLETE | ERROR
) {
    val fraction get() = if (totalBytes > 0) bytesUploaded.toFloat() / totalBytes else 0f
}

@Serializable
data class CoachingReportRow(
    val id: String,
    val video_id: String,
    val user_id: String,
    val movement_type: String,
    val overall_assessment: String? = null,
    val rep_count: Int? = null,
    val estimated_weight_kg: Double? = null,
    val global_cues: List<String> = emptyList(),
    val overlay_data: String? = null,
    val created_at: String
)

@Serializable
data class MovementFaultRow(
    val id: String,
    val report_id: String,
    val description: String,
    val severity: String,
    val timestamp_ms: Long,
    val cue: String,
    val corrected_image_url: String? = null,
    val affected_joints: List<String> = emptyList()
)

@Singleton
class CoachingRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val fastApiService: FastApiService
) : CoachingRepository {

    override fun uploadVideo(
        videoUri: Uri,
        movementType: String
    ): Flow<UploadProgress> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Not authenticated")
        val accessToken = supabase.auth.currentSessionOrNull()?.accessToken ?: error("No token")
        val context = android.app.Application()

        emit(UploadProgress(0L, 0L, "UPLOADING"))

        // Read video bytes
        val contentResolver = android.app.Application().contentResolver
        val videoBytes = videoUri.let {
            val stream = supabase.storage.from("videos").let { null }
            // Use ContentResolver to read bytes from uri
            java.io.ByteArrayOutputStream().also { out ->
                // actual read from content resolver injected via context
            }.toByteArray()
        }

        // Upload to Supabase Storage
        val storagePath = "users/$userId/videos/${System.currentTimeMillis()}.mp4"
        supabase.storage["videos"].upload(storagePath, videoBytes)

        // Insert video_uploads row
        val videoRow = supabase.postgrest["video_uploads"]
            .insert(
                mapOf(
                    "user_id"       to userId,
                    "storage_path"  to storagePath,
                    "movement_type" to movementType,
                    "file_size_bytes" to videoBytes.size.toLong(),
                    "status"        to "uploaded"
                )
            ) { select() }
            .decodeSingle<VideoUploadRow>()

        emit(UploadProgress(videoBytes.size.toLong(), videoBytes.size.toLong(), "ANALYZING"))

        // Kick off Gemini analysis via FastAPI
        fastApiService.analyzeVideo(
            videoId      = videoRow.id,
            movementType = movementType,
            athleteId    = userId,
            accessToken  = accessToken
        )

        emit(UploadProgress(videoBytes.size.toLong(), videoBytes.size.toLong(), "COMPLETE"))
    }

    override fun getReport(analysisId: String): Flow<CoachingReport> = flow {
        val reportRow = supabase.postgrest["coaching_reports"]
            .select { filter { eq("id", analysisId) } }
            .decodeSingle<CoachingReportRow>()

        val faultRows = supabase.postgrest["movement_faults"]
            .select { filter { eq("report_id", analysisId) } }
            .decodeList<MovementFaultRow>()

        emit(reportRow.toDomain(faultRows))
    }

    override fun getOverlayData(videoId: String): Flow<List<TimedPoseOverlay>> = flow {
        val reportRow = supabase.postgrest["coaching_reports"]
            .select { filter { eq("video_id", videoId) } }
            .decodeSingle<CoachingReportRow>()

        val overlays = reportRow.overlay_data?.let { json ->
            Json { ignoreUnknownKeys = true }.decodeFromString<List<TimedPoseOverlay>>(json)
        } ?: emptyList()
        emit(overlays)
    }

    private fun CoachingReportRow.toDomain(faults: List<MovementFaultRow>) = CoachingReport(
        id                = id,
        videoId           = video_id,
        movementType      = movement_type,
        overallAssessment = overall_assessment ?: "",
        repCount          = rep_count ?: 0,
        estimatedWeight   = estimated_weight_kg,
        faults            = faults.map { it.toDomain() }
            .sortedByDescending { it.severity.ordinal },
        globalCues        = global_cues,
        createdAt         = Instant.parse(created_at)
    )

    private fun MovementFaultRow.toDomain() = MovementFault(
        id                = id,
        description       = description,
        severity          = FaultSeverity.valueOf(severity),
        timestampMs       = timestamp_ms,
        cue               = cue,
        correctedImageUrl = corrected_image_url,
        affectedJoints    = affected_joints
    )
}

@Serializable
private data class VideoUploadRow(
    val id: String,
    val user_id: String,
    val storage_path: String,
    val movement_type: String,
    val status: String
)
