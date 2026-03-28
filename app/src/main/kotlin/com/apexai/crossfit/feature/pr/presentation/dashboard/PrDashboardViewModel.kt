package com.apexai.crossfit.feature.pr.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.feature.pr.domain.PrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrDashboardUiState(
    val prsByCategory: Map<String, List<PersonalRecord>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PrDashboardViewModel @Inject constructor(
    private val repository: PrRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrDashboardUiState())
    val uiState: StateFlow<PrDashboardUiState> = _uiState.asStateFlow()

    init { loadPrs() }

    private fun loadPrs() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllPrs(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { prsMap ->
                    _uiState.update { it.copy(prsByCategory = prsMap, isLoading = false) }
                }
        }
    }
}
