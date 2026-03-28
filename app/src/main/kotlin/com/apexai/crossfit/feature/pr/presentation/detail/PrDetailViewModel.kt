package com.apexai.crossfit.feature.pr.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.Movement
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
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
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class PrDetailUiState(
    val movement: Movement? = null,
    val currentPr: PersonalRecord? = null,
    val prHistory: List<PrHistoryEntry> = emptyList(),
    val filteredHistory: List<PrHistoryEntry> = emptyList(),
    val selectedTimeRangeIndex: Int = 1, // default 6M
    val isLoading: Boolean = true
)

@HiltViewModel
class PrDetailViewModel @Inject constructor(
    private val repository: PrRepository,
    private val supabase: SupabaseClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movementId: String = checkNotNull(savedStateHandle["movementId"])

    private val _uiState = MutableStateFlow(PrDetailUiState())
    val uiState: StateFlow<PrDetailUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            repository.getPrHistory(userId, movementId)
                .catch { e -> _uiState.update { it.copy(isLoading = false) } }
                .collect { history ->
                    val current = history.maxByOrNull { it.value }
                    _uiState.update {
                        it.copy(
                            prHistory   = history,
                            filteredHistory = filterHistory(history, it.selectedTimeRangeIndex),
                            isLoading   = false,
                            currentPr   = current?.let { e ->
                                PersonalRecord(
                                    id           = "",
                                    userId       = userId,
                                    movementId   = movementId,
                                    movementName = "",
                                    category     = "",
                                    value        = e.value,
                                    unit         = e.unit,
                                    achievedAt   = e.achievedAt
                                )
                            }
                        )
                    }
                }
        }
    }

    fun selectTimeRange(index: Int) {
        _uiState.update {
            it.copy(
                selectedTimeRangeIndex = index,
                filteredHistory = filterHistory(
The directory doesn't exist yet. All files are being output as a structured Markdown document. Continuing from the cut point in `PrDetailViewModel.kt`:
