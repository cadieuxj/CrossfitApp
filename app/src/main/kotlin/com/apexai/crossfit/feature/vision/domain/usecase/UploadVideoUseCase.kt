package com.apexai.crossfit.feature.vision.domain.usecase

import android.net.Uri
import com.apexai.crossfit.feature.vision.data.UploadProgress
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadVideoUseCase @Inject constructor(
    private val repository: CoachingRepository
) {
    operator fun invoke(videoUri: Uri, movementType: String): Flow<UploadProgress> =
        repository.uploadVideo(videoUri, movementType)
}
