package com.apexai.crossfit.feature.wod.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodHistoryUiState(
    val isLoading: Boolean = true,
    val results: List<WorkoutResult> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class WodHistoryViewModel @Inject constructor(
    private val repository: WodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WodHistoryUiState())
    val uiState: StateFlow<WodHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getWorkoutHistory().collect { result ->
                result.onSuccess { results ->
                    _uiState.update { it.copy(isLoading = false, results = results) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }
}
