package com.apexai.crossfit.feature.readiness.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.ReadinessScore
import com.apexai.crossfit.core.domain.model.ReadinessZone
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import com.apexai.crossfit.feature.readiness.domain.usecase.SyncHealthDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ReadinessUiState(
    val readinessScore: Float? = null,
    val readinessZone: ReadinessZone? = null,
    val acuteLoad: Float? = null,
    val chronicLoad: Float? = null,
    val latestHrv: Int? = null,
    val sleepDurationMinutes: Int? = null,
    val restingHr: Int? = null,
    val recommendation: String = "",
    val healthConnectPermissionsGranted: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
)

sealed interface ReadinessEvent {
    data object RefreshClicked    : ReadinessEvent
    data object SyncHealthData    : ReadinessEvent
    data object RequestPermissions : ReadinessEvent
}

sealed interface ReadinessEffect {
    data object NavigateToHealthConnectSetup : ReadinessEffect
}

@HiltViewModel
class ReadinessViewModel @Inject constructor(
    private val repository: ReadinessRepository,
    private val syncHealthDataUseCase: SyncHealthDataUseCase,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadinessUiState())
    val uiState: StateFlow<ReadinessUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ReadinessEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        checkPermissions()
        loadReadiness()
    }

    fun onEvent(event: ReadinessEvent) {
        when (event) {
            ReadinessEvent.RefreshClicked     -> loadReadiness()
            ReadinessEvent.SyncHealthData     -> syncData()
            ReadinessEvent.RequestPermissions -> viewModelScope.launch {
                _effects.send(ReadinessEffect.NavigateToHealthConnectSetup)
            }
        }
    }

    private fun checkPermissions() {
        viewModelScope.launch {
            val granted = repository.checkHealthConnectPermissions()
            _uiState.update { it.copy(healthConnectPermissionsGranted = granted) }
        }
    }

    private fun loadReadiness() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getReadinessScore(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { score ->
                    _uiState.update {
                        it.copy(
                            readinessScore        = score.acwr,
                            readinessZone         = score.zone,
                            acuteLoad             = score.acuteLoad,
                            chronicLoad           = score.chronicLoad,
                            latestHrv             = score.hrvComponent,
                            sleepDurationMinutes  = score.sleepDurationMinutes,
                            restingHr             = score.restingHr,
                            recommendation        = score.recommendation,
                            isLoading             = false
                        )
                    }
                }
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            syncHealthDataUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isSyncing = false, lastSyncedAt = Instant.now()) }
                    loadReadiness()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSyncing = false, error = e.message) }
                }
        }
    }
}
