package com.apexai.crossfit.feature.readiness.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface WellnessCheckInEffect {
    data object NavigateBack       : WellnessCheckInEffect
    data class ShowError(val msg: String) : WellnessCheckInEffect
}

@HiltViewModel
class WellnessCheckInViewModel @Inject constructor(
    private val repository: ReadinessRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _effects = Channel<WellnessCheckInEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Persists the morning wellness check-in values to the health_snapshots table.
     * Merges with existing biometric data for today's date rather than overwriting it.
     */
    fun submit(soreness: Int, perceivedReadiness: Int, mood: Int) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            val snapshot = HealthSnapshot(
                userId              = userId,
                hrvRmssd            = null,
                sleepDurationMinutes = null,
                deepSleepMinutes    = null,
                remSleepMinutes     = null,
                restingHr           = null,
                capturedAt          = Instant.now(),
                sorenessScore       = soreness,
                perceivedReadiness  = perceivedReadiness,
                moodScore           = mood
            )
            repository.syncHealthSnapshot(snapshot)
                .onSuccess { _effects.send(WellnessCheckInEffect.NavigateBack) }
                .onFailure { e ->
                    _effects.send(WellnessCheckInEffect.ShowError(e.message ?: "Failed to save check-in"))
                }
        }
    }
}
