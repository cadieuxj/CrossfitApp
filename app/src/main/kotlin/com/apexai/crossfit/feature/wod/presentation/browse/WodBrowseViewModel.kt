package com.apexai.crossfit.feature.wod.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.wod.domain.usecase.GetAllWodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodBrowseUiState(
    val isLoading: Boolean = true,
    val allWorkouts: List<WorkoutSummary> = emptyList(),
    val filteredWorkouts: List<WorkoutSummary> = emptyList(),
    val searchQuery: String = "",
    val selectedTimeDomain: TimeDomain? = null,
    val error: String? = null
)

@HiltViewModel
class WodBrowseViewModel @Inject constructor(
    private val getAllWodsUseCase: GetAllWodsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WodBrowseUiState())
    val uiState: StateFlow<WodBrowseUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            getAllWodsUseCase().collect { result ->
                result.onSuccess { workouts ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allWorkouts = workouts,
                            filteredWorkouts = applyFilters(workouts, it.searchQuery, it.selectedTimeDomain)
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredWorkouts = applyFilters(state.allWorkouts, query, state.selectedTimeDomain)
            )
        }
    }

    fun onTimeDomainFilterSelected(domain: TimeDomain?) {
        _uiState.update { state ->
            state.copy(
                selectedTimeDomain = domain,
                filteredWorkouts = applyFilters(state.allWorkouts, state.searchQuery, domain)
            )
        }
    }

    private fun applyFilters(
        workouts: List<WorkoutSummary>,
        query: String,
        domain: TimeDomain?
    ): List<WorkoutSummary> {
        return workouts.filter { wod ->
            val matchesSearch = query.isBlank() || wod.name.contains(query, ignoreCase = true)
            val matchesDomain = domain == null || wod.timeDomain == domain
            matchesSearch && matchesDomain
        }
    }
}
