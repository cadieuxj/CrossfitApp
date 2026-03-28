package com.apexai.crossfit.feature.vision.presentation.coaching

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CoachingReport
import com.apexai.crossfit.core.domain.model.MovementFault
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnalysisStatus { IDLE, UPLOADING, ANALYZING, COMPLETE, ERROR }

data class CoachingUiState(
    val analysisStatus: AnalysisStatus = AnalysisStatus.IDLE,
    val uploadProgress: Float = 0f,
    val report: CoachingReport? = null,
    val selectedFault: MovementFault? = null,
    val error: String? = null
)

sealed interface CoachingEvent {
    data object RetryAnalysis : CoachingEvent
}

sealed interface CoachingEffect {
    data class NavigateToPlayback(val videoId: String, val timestampMs: Long) : CoachingEffect
}

@HiltViewModel
class CoachingViewModel @Inject constructor(
    private val repository: CoachingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val analysisId: String = checkNotNull(savedStateHandle["analysisId"])

    private val _uiState = MutableStateFlow(CoachingUiState())
    val uiState: StateFlow<CoachingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CoachingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init { loadReport() }

    fun onEvent(event: CoachingEvent) {
        when (event) {
            CoachingEvent.RetryAnalysis -> {
                _uiState.update { it.copy(analysisStatus = AnalysisStatus.IDLE, error = null) }
                loadReport()
            }
        }
    }

    fun onFaultSelected(fault: MovementFault) {
        _uiState.update { it.copy(selectedFault = fault) }
        viewModelScope.launch {
            val report = _uiState.value.report ?: return@launch
            _effects.send(CoachingEffect.NavigateToPlayback(report.videoId, fault.timestampMs))
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(analysisStatus = AnalysisStatus.ANALYZING) }
            repository.getReport(analysisId)
                .catch { e ->
                    _uiState.update { it.copy(analysisStatus = AnalysisStatus.ERROR, error = e.message) }
                }
                .collect { report ->
                    _uiState.update {
                        it.copy(analysisStatus = AnalysisStatus.COMPLETE, report = report)
                    }
                }
        }
    }
}
