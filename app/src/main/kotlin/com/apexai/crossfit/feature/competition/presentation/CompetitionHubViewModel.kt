package com.apexai.crossfit.feature.competition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CompetitionEvent
import com.apexai.crossfit.core.domain.model.CompetitionStatus
import com.apexai.crossfit.feature.competition.domain.CompetitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompetitionHubUiState(
    val events: List<CompetitionEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val activeEvents get() = events.filter { it.status == CompetitionStatus.ACTIVE }
    val upcomingEvents get() = events.filter { it.status == CompetitionStatus.UPCOMING }
    val completedEvents get() = events.filter { it.status == CompetitionStatus.COMPLETED }
}

@HiltViewModel
class CompetitionHubViewModel @Inject constructor(
    private val repository: CompetitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetitionHubUiState())
    val uiState: StateFlow<CompetitionHubUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getEvents()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { events ->
                    _uiState.update { it.copy(events = events, isLoading = false) }
                }
        }
    }
}
