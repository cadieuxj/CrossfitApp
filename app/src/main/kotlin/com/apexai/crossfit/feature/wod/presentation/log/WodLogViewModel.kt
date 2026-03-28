package com.apexai.crossfit.feature.wod.presentation.log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ScoringMetric
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.feature.competition.domain.CompetitionRepository
import com.apexai.crossfit.feature.wod.domain.WodRepository
import com.apexai.crossfit.feature.wod.domain.usecase.SubmitResultUseCase
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

data class WodLogUiState(
    val workout: Workout? = null,
    val score: String = "",
    val rxd: Boolean = true,
    val notes: String = "",
    val rpe: Int? = null,
    val sessionDurationMinutes: Int? = null,
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = true,
    val newPrs: List<PersonalRecord> = emptyList(),
    val error: String? = null,
    // Competition mode
    val isActiveCompetitionEvent: Boolean = false,
    val activeCompetitionName: String? = null,
    val isOfficialSubmission: Boolean = false
)

sealed interface WodLogEvent {
    data class ScoreChanged(val score: String)          : WodLogEvent
    data class RxdToggled(val rxd: Boolean)             : WodLogEvent
    data class NotesChanged(val notes: String)          : WodLogEvent
    data class RpeSelected(val rpe: Int)                : WodLogEvent
    data class DurationChanged(val minutes: Int)        : WodLogEvent
    data class OfficialSubmissionToggled(val isOfficial: Boolean) : WodLogEvent
    data object SubmitClicked                           : WodLogEvent
    data object DismissPrSheet                          : WodLogEvent
}

sealed interface WodLogEffect {
    data class PrAchieved(val prs: List<PersonalRecord>) : WodLogEffect
    data object NavigateBack                             : WodLogEffect
    data class ShowError(val message: String)            : WodLogEffect
}

@HiltViewModel
class WodLogViewModel @Inject constructor(
    private val submitResultUseCase: SubmitResultUseCase,
    private val repository: WodRepository,
    private val competitionRepository: CompetitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wodId: String = checkNotNull(savedStateHandle["wodId"])

    private val _uiState = MutableStateFlow(WodLogUiState())
    val uiState: StateFlow<WodLogUiState> = _uiState.asStateFlow()

    private val _effects = Channel<WodLogEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getWorkoutById(wodId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { workout ->
                    _uiState.update { it.copy(workout = workout, isLoading = false) }
                }
        }
        checkActiveCompetition()
    }

    private fun checkActiveCompetition() {
        viewModelScope.launch {
            competitionRepository.getActiveEvents()
                .catch { }
                .collect { activeEvents ->
                    val event = activeEvents.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isActiveCompetitionEvent = event != null,
                            activeCompetitionName = event?.name
                        )
                    }
                }
        }
    }

    fun onEvent(event: WodLogEvent) {
        when (event) {
            is WodLogEvent.ScoreChanged    -> _uiState.update { it.copy(score = event.score, error = null) }
            is WodLogEvent.RxdToggled      -> _uiState.update { it.copy(rxd = event.rxd) }
            is WodLogEvent.NotesChanged    -> _uiState.update { it.copy(notes = event.notes) }
            is WodLogEvent.RpeSelected     -> _uiState.update { it.copy(rpe = event.rpe) }
            is WodLogEvent.DurationChanged -> _uiState.update { it.copy(sessionDurationMinutes = event.minutes.coerceIn(1, 240)) }
            is WodLogEvent.OfficialSubmissionToggled -> _uiState.update { it.copy(isOfficialSubmission = event.isOfficial) }
            is WodLogEvent.SubmitClicked   -> submit()
            is WodLogEvent.DismissPrSheet -> viewModelScope.launch {
                _effects.send(WodLogEffect.NavigateBack)
            }
        }
    }

    private fun parseScoreNumeric(score: String, metric: ScoringMetric?): Double? =
        when (metric) {
            ScoringMetric.ROUNDS_PLUS_REPS -> {
                val parts = score.split("+")
                val rounds = parts.getOrNull(0)?.trim()?.toLongOrNull() ?: return null
                val reps   = parts.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L
                // Encode as rounds * 1000 + reps for sortable integer comparison
                (rounds * 1_000L + reps).toDouble()
            }
            ScoringMetric.TIME -> {
                // "MM:SS" → total seconds
                val parts = score.split(":")
                val minutes = parts.getOrNull(0)?.trim()?.toLongOrNull() ?: return null
                val seconds = parts.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L
                (minutes * 60L + seconds).toDouble()
            }
            else -> score.toDoubleOrNull()
        }

    private fun submit() {
        val state = _uiState.value
        if (state.score.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your score") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            submitResultUseCase(
                WorkoutResultInput(
                    workoutId              = wodId,
                    score                  = state.score,
                    scoreNumeric           = parseScoreNumeric(state.score, state.workout?.scoringMetric),
                    rxd                    = state.rxd,
                    notes                  = state.notes.ifBlank { null },
                    rpe                    = state.rpe,
                    sessionDurationMinutes = state.sessionDurationMinutes,
                    isOfficialSubmission   = state.isOfficialSubmission
                )
            ).onSuccess { result ->
                _uiState.update { it.copy(isSubmitting = false) }
                if (result.newPrs.isNotEmpty()) {
                    _effects.send(WodLogEffect.PrAchieved(result.newPrs))
                } else {
                    _effects.send(WodLogEffect.NavigateBack)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSubmitting = false, error = error.message) }
                _effects.send(WodLogEffect.ShowError(error.message ?: "Failed to submit"))
            }
        }
    }
}
