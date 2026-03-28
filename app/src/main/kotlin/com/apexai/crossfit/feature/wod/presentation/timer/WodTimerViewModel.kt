package com.apexai.crossfit.feature.wod.presentation.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.TimeDomain
import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WodTimerUiState(
    val workout: Workout? = null,
    val elapsedMillis: Long = 0L,
    val currentRound: Int = 1,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val currentIntervalSecondsRemaining: Int = 0,
    val tabataIsWorkInterval: Boolean = true,
    val tabataCompletedIntervals: Int = 0,
    val isLoading: Boolean = true
)

sealed interface WodTimerEvent {
    data object StartPause : WodTimerEvent
    data object Reset      : WodTimerEvent
    data object Complete   : WodTimerEvent
}

@HiltViewModel
class WodTimerViewModel @Inject constructor(
    private val repository: WodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wodId: String = checkNotNull(savedStateHandle["wodId"])

    private val _uiState = MutableStateFlow(WodTimerUiState())
    val uiState: StateFlow<WodTimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val tickIntervalMs = 100L

    init {
        viewModelScope.launch {
            repository.getWorkoutById(wodId)
                .catch {}
                .collect { workout ->
                    val initialInterval = when (workout.timeDomain) {
                        TimeDomain.EMOM   -> 60
                        TimeDomain.TABATA -> 20
                        else              -> 0
                    }
                    _uiState.update {
                        it.copy(
                            workout = workout,
                            isLoading = false,
                            currentIntervalSecondsRemaining = initialInterval
                        )
                    }
                }
        }
    }

    fun onEvent(event: WodTimerEvent) {
        when (event) {
            is WodTimerEvent.StartPause -> toggleTimer()
            is WodTimerEvent.Reset      -> resetTimer()
            is WodTimerEvent.Complete   -> completeWorkout()
        }
    }

    private fun toggleTimer() {
        val state = _uiState.value
        if (state.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(tickIntervalMs)
                tick()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun tick() {
        val state = _uiState.value
        val workout = state.workout ?: return

        when (workout.timeDomain) {
            TimeDomain.AMRAP -> tickAmrap(state, workout)
            TimeDomain.EMOM  -> tickEmom(state)
            TimeDomain.RFT   -> tickRft(state)
            TimeDomain.TABATA -> tickTabata(state)
        }
    }

    private fun tickAmrap(state: WodTimerUiState, workout: Workout) {
        val timeCap = workout.timeCap?.toMillis() ?: Long.MAX_VALUE
        val newElapsed = state.elapsedMillis + tickIntervalMs
        if (newElapsed >= timeCap) {
            _uiState.update { it.copy(elapsedMillis = timeCap, isRunning = false, isComplete = true) }
            timerJob?.cancel()
        } else {
            _uiState.update { it.copy(elapsedMillis = newElapsed) }
        }
    }

    private fun tickEmom(state: WodTimerUiState) {
        val newElapsed = state.elapsedMillis + tickIntervalMs
        val secondsInMinute = ((newElapsed / 1000) % 60).toInt()
        val secondsRemaining = 60 - secondsInMinute
        val newRound = (newElapsed / 60_000).toInt() + 1
        val totalRounds = state.workout?.rounds ?: Int.MAX_VALUE
        if (newRound > totalRounds) {
            _uiState.update { it.copy(isRunning = false, isComplete = true) }
            timerJob?.cancel()
        } else {
            _uiState.update {
                it.copy(
                    elapsedMillis = newElapsed,
                    currentIntervalSecondsRemaining = secondsRemaining,
                    currentRound = newRound
                )
            }
        }
    }

    private fun tickRft(state: WodTimerUiState) {
        _uiState.update { it.copy(elapsedMillis = state.elapsedMillis + tickIntervalMs) }
    }

    private fun tickTabata(state: WodTimerUiState) {
        val intervalDuration = if (state.tabataIsWorkInterval) 20_000L else 10_000L
        val positionInInterval = state.elapsedMillis % (20_000L + 10_000L)
        val newElapsed = state.elapsedMillis + tickIntervalMs
        val totalTabataMs = 8L * (20_000L + 10_000L)

        if (newElapsed >= totalTabataMs) {
            _uiState.update { it.copy(isRunning = false, isComplete = true) }
            timerJob?.cancel()
            return
        }

        val isWorkInterval = (newElapsed % (20_000L + 10_000L)) < 20_000L
        val msInCurrentInterval = newElapsed % (20_000L + 10_000L)
        val remaining = if (isWorkInterval) {
            ((20_000L - msInCurrentInterval) / 1000L).toInt().coerceAtLeast(0)
        } else {
            ((30_000L - msInCurrentInterval) / 1000L).toInt().coerceAtLeast(0)
        }
        val completedIntervals = (newElapsed / (20_000L + 10_000L)).toInt()

        _uiState.update {
            it.copy(
                elapsedMillis = newElapsed,
                tabataIsWorkInterval = isWorkInterval,
                currentIntervalSecondsRemaining = remaining,
                tabataCompletedIntervals = completedIntervals
            )
        }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        val initialInterval = when (_uiState.value.workout?.timeDomain) {
            TimeDomain.EMOM   -> 60
            TimeDomain.TABATA -> 20
            else              -> 0
        }
        _uiState.update {
            it.copy(
                elapsedMillis = 0L,
                currentRound  = 1,
                isRunning     = false,
                isComplete    = false,
                currentIntervalSecondsRemaining = initialInterval,
                tabataIsWorkInterval = true,
                tabataCompletedIntervals = 0
            )
        }
    }

    private fun completeWorkout() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false, isComplete = true) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
