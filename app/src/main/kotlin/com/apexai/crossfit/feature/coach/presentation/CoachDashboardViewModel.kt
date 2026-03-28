package com.apexai.crossfit.feature.coach.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.feature.coach.domain.AthleteInfo
import com.apexai.crossfit.feature.coach.domain.CoachRepository
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

data class CoachDashboardUiState(
    val athletes: List<AthleteInfo> = emptyList(),
    val selectedAthleteId: String? = null,
    val athleteResults: List<WorkoutResult> = emptyList(),
    val isLoadingAthletes: Boolean = true,
    val isLoadingResults: Boolean = false,
    val error: String? = null,
    // Note editing
    val editingResultId: String? = null,
    val noteInput: String = "",
    val isSavingNote: Boolean = false
) {
    val selectedAthlete get() = athletes.firstOrNull { it.userId == selectedAthleteId }
}

sealed interface CoachDashboardEvent {
    data class SelectAthlete(val athleteId: String)    : CoachDashboardEvent
    data class StartEditNote(val resultId: String, val existingNote: String) : CoachDashboardEvent
    data class NoteChanged(val note: String)           : CoachDashboardEvent
    data object SaveNote                               : CoachDashboardEvent
    data object CancelEditNote                         : CoachDashboardEvent
}

sealed interface CoachDashboardEffect {
    data class ShowError(val message: String) : CoachDashboardEffect
    data object NoteSaved                     : CoachDashboardEffect
}

@HiltViewModel
class CoachDashboardViewModel @Inject constructor(
    private val repository: CoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachDashboardUiState())
    val uiState: StateFlow<CoachDashboardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CoachDashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadAthletes()
    }

    fun onEvent(event: CoachDashboardEvent) {
        when (event) {
            is CoachDashboardEvent.SelectAthlete -> selectAthlete(event.athleteId)
            is CoachDashboardEvent.StartEditNote -> _uiState.update {
                it.copy(editingResultId = event.resultId, noteInput = event.existingNote)
            }
            is CoachDashboardEvent.NoteChanged  -> _uiState.update { it.copy(noteInput = event.note) }
            CoachDashboardEvent.SaveNote        -> saveNote()
            CoachDashboardEvent.CancelEditNote  -> _uiState.update {
                it.copy(editingResultId = null, noteInput = "")
            }
        }
    }

    private fun loadAthletes() {
        viewModelScope.launch {
            repository.getMyAthletes()
                .catch { e -> _uiState.update { it.copy(isLoadingAthletes = false, error = e.message) } }
                .collect { athletes ->
                    _uiState.update { it.copy(athletes = athletes, isLoadingAthletes = false) }
                    // Auto-select first athlete
                    if (athletes.isNotEmpty() && _uiState.value.selectedAthleteId == null) {
                        selectAthlete(athletes.first().userId)
                    }
                }
        }
    }

    private fun selectAthlete(athleteId: String) {
        _uiState.update { it.copy(selectedAthleteId = athleteId, isLoadingResults = true) }
        viewModelScope.launch {
            repository.getAthleteResults(athleteId)
                .catch { e -> _uiState.update { it.copy(isLoadingResults = false, error = e.message) } }
                .collect { results ->
                    _uiState.update { it.copy(athleteResults = results, isLoadingResults = false) }
                }
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        val resultId = state.editingResultId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingNote = true) }
            repository.addCoachNote(resultId, state.noteInput.trim()).onSuccess {
                _uiState.update {
                    it.copy(
                        isSavingNote = false, editingResultId = null, noteInput = "",
                        athleteResults = it.athleteResults.map { r ->
                            if (r.id == resultId) r.copy(coachNote = state.noteInput.trim()) else r
                        }
                    )
                }
                _effects.send(CoachDashboardEffect.NoteSaved)
            }.onFailure { e ->
                _uiState.update { it.copy(isSavingNote = false) }
                _effects.send(CoachDashboardEffect.ShowError(e.message ?: "Save failed"))
            }
        }
    }
}
