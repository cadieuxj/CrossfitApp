package com.apexai.crossfit.feature.competition.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CompetitionEvent
import com.apexai.crossfit.core.domain.model.CompetitionStanding
import com.apexai.crossfit.core.domain.model.CompetitionStandingInput
import com.apexai.crossfit.feature.competition.domain.CompetitionRepository
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

data class CompetitionDetailUiState(
    val event: CompetitionEvent? = null,
    val standings: List<CompetitionStanding> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    // Entry sheet
    val showEntrySheet: Boolean = false,
    val entryWorkoutName: String = "",
    val entryScore: String = "",
    val entryDivision: String = "RX",
    val entryRankOverall: String = "",
    val entryPercentile: String = ""
)

sealed interface CompetitionDetailEvent {
    data object EnterStanding                     : CompetitionDetailEvent
    data object DismissEntrySheet                 : CompetitionDetailEvent
    data class WorkoutNameChanged(val name: String) : CompetitionDetailEvent
    data class ScoreChanged(val score: String)    : CompetitionDetailEvent
    data class DivisionChanged(val div: String)   : CompetitionDetailEvent
    data class RankChanged(val rank: String)      : CompetitionDetailEvent
    data class PercentileChanged(val pct: String) : CompetitionDetailEvent
    data object SubmitStanding                    : CompetitionDetailEvent
    data class DeleteStanding(val id: String)     : CompetitionDetailEvent
}

sealed interface CompetitionDetailEffect {
    data class ShowError(val message: String) : CompetitionDetailEffect
    data object StandingSaved                 : CompetitionDetailEffect
}

@HiltViewModel
class CompetitionDetailViewModel @Inject constructor(
    private val repository: CompetitionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(CompetitionDetailUiState())
    val uiState: StateFlow<CompetitionDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CompetitionDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadEvent()
        loadStandings()
    }

    fun onEvent(event: CompetitionDetailEvent) {
        when (event) {
            CompetitionDetailEvent.EnterStanding    -> _uiState.update { it.copy(showEntrySheet = true) }
            CompetitionDetailEvent.DismissEntrySheet -> _uiState.update { it.copy(showEntrySheet = false) }
            is CompetitionDetailEvent.WorkoutNameChanged -> _uiState.update { it.copy(entryWorkoutName = event.name) }
            is CompetitionDetailEvent.ScoreChanged   -> _uiState.update { it.copy(entryScore = event.score) }
            is CompetitionDetailEvent.DivisionChanged -> _uiState.update { it.copy(entryDivision = event.div) }
            is CompetitionDetailEvent.RankChanged    -> _uiState.update { it.copy(entryRankOverall = event.rank) }
            is CompetitionDetailEvent.PercentileChanged -> _uiState.update { it.copy(entryPercentile = event.pct) }
            CompetitionDetailEvent.SubmitStanding    -> submitStanding()
            is CompetitionDetailEvent.DeleteStanding -> deleteStanding(event.id)
        }
    }

    private fun loadEvent() {
        viewModelScope.launch {
            repository.getEventById(eventId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { event ->
                    _uiState.update { it.copy(event = event, isLoading = false) }
                }
        }
    }

    private fun loadStandings() {
        viewModelScope.launch {
            repository.getMyStandings(eventId)
                .catch { /* standings are non-critical */ }
                .collect { standings ->
                    _uiState.update { it.copy(standings = standings) }
                }
        }
    }

    private fun submitStanding() {
        val state = _uiState.value
        if (state.entryWorkoutName.isBlank() || state.entryScore.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val input = CompetitionStandingInput(
                eventId      = eventId,
                workoutName  = state.entryWorkoutName.trim(),
                score        = state.entryScore.trim(),
                scoreNumeric = state.entryScore.toDoubleOrNull(),
                division     = state.entryDivision,
                rankOverall  = state.entryRankOverall.toIntOrNull(),
                percentile   = state.entryPercentile.toDoubleOrNull()
            )
            repository.upsertStanding(input).onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false, showEntrySheet = false,
                        entryWorkoutName = "", entryScore = "",
                        entryDivision = "RX", entryRankOverall = "", entryPercentile = ""
                    )
                }
                _effects.send(CompetitionDetailEffect.StandingSaved)
                loadStandings()
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false) }
                _effects.send(CompetitionDetailEffect.ShowError(e.message ?: "Failed to save"))
            }
        }
    }

    private fun deleteStanding(standingId: String) {
        viewModelScope.launch {
            repository.deleteStanding(standingId).onSuccess {
                loadStandings()
            }.onFailure { e ->
                _effects.send(CompetitionDetailEffect.ShowError(e.message ?: "Failed to delete"))
            }
        }
    }
}
