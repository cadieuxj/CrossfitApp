package com.apexai.crossfit.feature.vision.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.media.PlayerPoolManager
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import com.apexai.crossfit.feature.vision.domain.usecase.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordingReviewUiState(
    val selectedMovement: Movement? = null,
    val playbackSpeed: Float = 1.0f,
    val isUploading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val uploadProgress: Float = 0f,
    val showGeminiConsentDialog: Boolean = false,
    val pendingVideoUri: String? = null,
    val error: String? = null
)

sealed interface RecordingReviewEffect {
    data class NavigateToReport(val analysisId: String) : RecordingReviewEffect
}

@HiltViewModel
class RecordingReviewViewModel @Inject constructor(
    val poolManager: PlayerPoolManager,
    private val uploadVideoUseCase: UploadVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingReviewUiState())
    val uiState: StateFlow<RecordingReviewUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RecordingReviewEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var uploadJob: Job? = null
    private var currentVideoUri: String = ""

    fun loadVideo(videoUri: String) {
        currentVideoUri = videoUri
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    /**
     * Step 1: Show Law 25 / PIPEDA consent dialog before sending video to Google Gemini.
     * The actual upload only starts after the user explicitly accepts via [confirmGeminiConsent].
     */
    fun requestGeminiConsent(videoUri: String) {
        _uiState.update { it.copy(showGeminiConsentDialog = true, pendingVideoUri = videoUri) }
    }

    fun dismissGeminiConsent() {
        _uiState.update { it.copy(showGeminiConsentDialog = false, pendingVideoUri = null) }
    }

    /** Step 2: Called after user accepts the consent dialog — proceeds with upload. */
    fun confirmGeminiConsent() {
        val uri = _uiState.value.pendingVideoUri ?: return
        _uiState.update { it.copy(showGeminiConsentDialog = false, pendingVideoUri = null) }
        analyzeVideo(uri)
    }

    private fun analyzeVideo(videoUri: String) {
        val movement = _uiState.value.selectedMovement ?: return
        uploadJob = viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            uploadVideoUseCase(
                android.net.Uri.parse(videoUri),
                movement.name
            ).catch { e ->
                _uiState.update { it.copy(isUploading = false, isAnalyzing = false, error = e.message) }
            }.collect { progress ->
                when (progress.status) {
                    "UPLOADING" -> _uiState.update {
                        it.copy(uploadProgress = progress.fraction, isUploading = true)
                    }
                    "ANALYZING" -> _uiState.update {
                        it.copy(isUploading = false, isAnalyzing = true)
                    }
                    "COMPLETE"  -> {
                        _uiState.update { it.copy(isUploading = false, isAnalyzing = false) }
                        // analysisId comes from the progress/report response
                        _effects.send(RecordingReviewEffect.NavigateToReport("pending_analysis_id"))
                    }
                }
            }
        }
    }

    fun cancelUpload() {
        uploadJob?.cancel()
        _uiState.update { it.copy(isUploading = false, isAnalyzing = false, uploadProgress = 0f) }
    }
}
