package com.apexai.crossfit.feature.vision.presentation.playback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.FaultMarker
import com.apexai.crossfit.core.domain.model.FaultSeverity
import com.apexai.crossfit.core.domain.model.TimedPoseOverlay
import com.apexai.crossfit.core.media.PlayerPoolManager
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoPlaybackUiState(
    val videoUrl: String? = null,
    val overlayData: List<TimedPoseOverlay> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val faultMarkers: List<FaultMarker> = emptyList(),
    val isLoading: Boolean = true,
    val repCount: Int = 0
)

@HiltViewModel
class VideoPlaybackViewModel @Inject constructor(
    val playerPoolManager: PlayerPoolManager,
    private val repository: CoachingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String   = checkNotNull(savedStateHandle["videoId"])
    private val startMs: Long     = savedStateHandle["timestamp"] ?: 0L

    private val _uiState = MutableStateFlow(VideoPlaybackUiState())
    val uiState: StateFlow<VideoPlaybackUiState> = _uiState.asStateFlow()

    val initialSeekPositionMs: Long get() = startMs

    init {
        loadOverlayData()
        loadReport()
    }

    private fun loadOverlayData() {
        viewModelScope.launch {
            repository.getOverlayData(videoId)
                .catch {}
                .collect { overlays ->
                    _uiState.update { it.copy(overlayData = overlays, isLoading = false) }
                }
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            repository.getReport(videoId)
                .catch {}
                .collect { report ->
                    val markers = report.faults.map {
                        FaultMarker(it.timestampMs, it.description, it.severity)
                    }
                    _uiState.update {
                        it.copy(
                            faultMarkers = markers,
                            repCount     = report.repCount
                        )
                    }
                }
        }
    }

    fun updatePosition(positionMs: Long, durationMs: Long, isPlaying: Boolean) {
        _uiState.update {
            it.copy(
                currentPositionMs = positionMs,
                durationMs        = durationMs,
                isPlaying         = isPlaying
            )
        }
    }

    /**
     * Finds the overlay frame nearest to the given player position (within 33ms / ~1 frame).
     */
    fun overlayForPosition(positionMs: Long): TimedPoseOverlay? =
        _uiState.value.overlayData
            .minByOrNull { kotlin.math.abs(it.timestampMs - positionMs) }
            ?.takeIf { kotlin.math.abs(it.timestampMs - positionMs) < 33L }

    fun faultAtPosition(positionMs: Long): FaultMarker? =
        _uiState.value.faultMarkers
            .firstOrNull { kotlin.math.abs(it.timestampMs - positionMs) <= 500L }

    fun previousFaultTimestamp(currentMs: Long): Long? =
        _uiState.value.faultMarkers
            .filter { it.timestampMs < currentMs - 500L }
            .maxByOrNull { it.timestampMs }?.timestampMs

    fun nextFaultTimestamp(currentMs: Long): Long? =
        _uiState.value.faultMarkers
            .filter { it.timestampMs > currentMs + 500L }
            .minByOrNull { it.timestampMs }?.timestampMs
}
