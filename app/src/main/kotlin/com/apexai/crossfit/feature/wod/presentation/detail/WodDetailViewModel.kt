package com.apexai.crossfit.feature.wod.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.core.domain.model.WorkoutMovement
import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodDetailUiState(
    val workout: Workout? = null,
    val movements: List<WorkoutMovement> = emptyList(),
    val recentResults: List<WorkoutResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WodDetailViewModel @Inject constructor(
    private val repository: WodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wodId: String = checkNotNull(savedStateHandle["wodId"])

    private val _uiState = MutableStateFlow(WodDetailUiState())
    val uiState: StateFlow<WodDetailUiState> = _uiState.asStateFlow()

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            repository.getWorkoutById(wodId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { workout ->
                    _uiState.update {
                        it.copy(
                            workout   = workout,
                            movements = workout.movements,
                            isLoading = false,
                            error     = null
                        )
                    }
                }
        }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadWorkout()
    }
}
