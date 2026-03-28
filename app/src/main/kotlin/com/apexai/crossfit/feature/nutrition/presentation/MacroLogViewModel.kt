package com.apexai.crossfit.feature.nutrition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CommonFood
import com.apexai.crossfit.core.domain.model.DailyMacroSummary
import com.apexai.crossfit.core.domain.model.MacroEntryInput
import com.apexai.crossfit.core.domain.model.MacroTargets
import com.apexai.crossfit.core.domain.model.MealType
import com.apexai.crossfit.feature.nutrition.domain.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MacroLogUiState(
    val today: LocalDate = LocalDate.now(),
    val summary: DailyMacroSummary? = null,
    val targets: MacroTargets? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Entry sheet
    val showEntrySheet: Boolean = false,
    val entryFoodName: String = "",
    val entryMealType: MealType = MealType.LUNCH,
    val entryCalories: String = "",
    val entryProtein: String = "",
    val entryCarbs: String = "",
    val entryFat: String = "",
    val entryNotes: String = "",
    val isSubmitting: Boolean = false,
    // Food search
    val foodQuery: String = "",
    val foodSuggestions: List<CommonFood> = emptyList()
)

sealed interface MacroLogEvent {
    data object AddEntry                              : MacroLogEvent
    data object DismissEntrySheet                    : MacroLogEvent
    data class FoodNameChanged(val name: String)     : MacroLogEvent
    data class MealTypeChanged(val type: MealType)   : MacroLogEvent
    data class CaloriesChanged(val value: String)    : MacroLogEvent
    data class ProteinChanged(val value: String)     : MacroLogEvent
    data class CarbsChanged(val value: String)       : MacroLogEvent
    data class FatChanged(val value: String)         : MacroLogEvent
    data class NotesChanged(val value: String)       : MacroLogEvent
    data class FoodSuggestionSelected(val food: CommonFood) : MacroLogEvent
    data object SubmitEntry                          : MacroLogEvent
    data class DeleteEntry(val entryId: String)      : MacroLogEvent
}

sealed interface MacroLogEffect {
    data class ShowError(val message: String) : MacroLogEffect
    data object EntrySaved                    : MacroLogEffect
}

@OptIn(FlowPreview::class)
@HiltViewModel
class MacroLogViewModel @Inject constructor(
    private val repository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacroLogUiState())
    val uiState: StateFlow<MacroLogUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MacroLogEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadData()
        observeSearch()
    }

    fun onEvent(event: MacroLogEvent) {
        when (event) {
            MacroLogEvent.AddEntry        -> _uiState.update { it.copy(showEntrySheet = true) }
            MacroLogEvent.DismissEntrySheet -> _uiState.update { it.copy(showEntrySheet = false, foodSuggestions = emptyList()) }
            is MacroLogEvent.FoodNameChanged -> {
                _uiState.update { it.copy(entryFoodName = event.name) }
                _searchQuery.value = event.name
            }
            is MacroLogEvent.MealTypeChanged  -> _uiState.update { it.copy(entryMealType = event.type) }
            is MacroLogEvent.CaloriesChanged  -> _uiState.update { it.copy(entryCalories = event.value) }
            is MacroLogEvent.ProteinChanged   -> _uiState.update { it.copy(entryProtein = event.value) }
            is MacroLogEvent.CarbsChanged     -> _uiState.update { it.copy(entryCarbs = event.value) }
            is MacroLogEvent.FatChanged       -> _uiState.update { it.copy(entryFat = event.value) }
            is MacroLogEvent.NotesChanged     -> _uiState.update { it.copy(entryNotes = event.value) }
            is MacroLogEvent.FoodSuggestionSelected -> applyFoodSuggestion(event.food)
            MacroLogEvent.SubmitEntry    -> submitEntry()
            is MacroLogEvent.DeleteEntry -> deleteEntry(event.entryId)
        }
    }

    private fun loadData() {
        val today = LocalDate.now()
        viewModelScope.launch {
            launch {
                repository.getTargets()
                    .catch { }
                    .collect { targets -> _uiState.update { it.copy(targets = targets) } }
            }
            launch {
                repository.getDailySummary(today)
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { summary -> _uiState.update { it.copy(summary = summary, isLoading = false) } }
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .collect { query ->
                    repository.searchCommonFoods(query)
                        .catch { }
                        .collect { foods -> _uiState.update { it.copy(foodSuggestions = foods) } }
                }
        }
    }

    private fun applyFoodSuggestion(food: CommonFood) {
        _uiState.update {
            it.copy(
                entryFoodName    = food.name,
                entryCalories    = food.calories.toString(),
                entryProtein     = food.proteinG.toString(),
                entryCarbs       = food.carbsG.toString(),
                entryFat         = food.fatG.toString(),
                foodSuggestions  = emptyList()
            )
        }
        _searchQuery.value = ""
    }

    private fun submitEntry() {
        val state = _uiState.value
        if (state.entryFoodName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            repository.logEntry(
                MacroEntryInput(
                    loggedDate = state.today,
                    mealType   = state.entryMealType,
                    foodName   = state.entryFoodName.trim(),
                    calories   = state.entryCalories.toIntOrNull() ?: 0,
                    proteinG   = state.entryProtein.toDoubleOrNull() ?: 0.0,
                    carbsG     = state.entryCarbs.toDoubleOrNull() ?: 0.0,
                    fatG       = state.entryFat.toDoubleOrNull() ?: 0.0,
                    notes      = state.entryNotes.ifBlank { null }
                )
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false, showEntrySheet = false,
                        entryFoodName = "", entryCalories = "", entryProtein = "",
                        entryCarbs = "", entryFat = "", entryNotes = "",
                        foodSuggestions = emptyList()
                    )
                }
                _effects.send(MacroLogEffect.EntrySaved)
                loadData()
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false) }
                _effects.send(MacroLogEffect.ShowError(e.message ?: "Failed to save"))
            }
        }
    }

    private fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteEntry(entryId).onSuccess { loadData() }
                .onFailure { e -> _effects.send(MacroLogEffect.ShowError(e.message ?: "Failed to delete")) }
        }
    }
}
