package com.apexai.crossfit.feature.vision.domain

import android.net.Uri
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.feature.vision.data.UploadProgress
import kotlinx.coroutines.flow.Flow

interface CoachingRepository {
    fun uploadVideo(videoUri: Uri, movementType: String): Flow<UploadProgress>
    fun getReport(analysisId: String): Flow<CoachingReport>
    fun getOverlayData(videoId: String): Flow<List<TimedPoseOverlay>>
}
