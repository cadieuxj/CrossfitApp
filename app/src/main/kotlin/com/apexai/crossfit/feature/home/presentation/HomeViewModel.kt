package com.apexai.crossfit.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.DailyMacroSummary
import com.apexai.crossfit.core.domain.model.MacroTargets
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.nutrition.domain.NutritionRepository
import com.apexai.crossfit.feature.pr.domain.PrRepository
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import com.apexai.crossfit.feature.wod.domain.usecase.GetTodayWodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayWod: WorkoutSummary? = null,
    val readiness: ReadinessScore? = null,
    val recentPrs: List<PersonalRecord> = emptyList(),
    val macroSummary: DailyMacroSummary? = null,
    val macroTargets: MacroTargets? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayWodUseCase: GetTodayWodUseCase,
    private val readinessRepository: ReadinessRepository,
    private val prRepository: PrRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val wodDeferred = async {
                    var wod: WorkoutSummary? = null
                    getTodayWodUseCase().collect { result ->
                        result.onSuccess { wod = it }
                    }
                    wod
                }
                val readinessDeferred = async {
                    var score: ReadinessScore? = null
                    readinessRepository.getLatestReadiness().collect { result ->
                        result.onSuccess { score = it }
                    }
                    score
                }
                val prsDeferred = async {
                    var prs: List<PersonalRecord> = emptyList()
                    prRepository.getAllPrs().collect { result ->
                        result.onSuccess { grouped ->
                            prs = grouped.values.flatten()
                                .sortedByDescending { it.achievedAt }
                                .take(3)
                        }
                    }
                    prs
                }
                val macroDeferred = async {
                    var summary: DailyMacroSummary? = null
                    nutritionRepository.getDailySummary(LocalDate.now())
                        .catch { }
                        .collect { summary = it }
                    summary
                }
                val targetsDeferred = async {
                    var targets: MacroTargets? = null
                    nutritionRepository.getTargets()
                        .catch { }
                        .collect { targets = it }
                    targets
                }
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        todayWod     = wodDeferred.await(),
                        readiness    = readinessDeferred.await(),
                        recentPrs    = prsDeferred.await(),
                        macroSummary = macroDeferred.await(),
                        macroTargets = targetsDeferred.await()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
