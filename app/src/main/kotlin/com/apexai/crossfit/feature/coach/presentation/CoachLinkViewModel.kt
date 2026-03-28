package com.apexai.crossfit.feature.coach.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.core.domain.model.CoachConnection
import com.apexai.crossfit.feature.coach.domain.CoachRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoachLinkUiState(
    val connections: List<CoachConnection> = emptyList(),
    val isLoading: Boolean = true,
    val isLinking: Boolean = false,
    val inviteCode: String = "",
    val error: String? = null
)

sealed interface CoachLinkEvent {
    data class InviteCodeChanged(val code: String) : CoachLinkEvent
    data object LinkCoach                          : CoachLinkEvent
    data class UnlinkCoach(val connectionId: String) : CoachLinkEvent
}

sealed interface CoachLinkEffect {
    data object CoachLinked                       : CoachLinkEffect
    data class ShowError(val message: String)     : CoachLinkEffect
}

@HiltViewModel
class CoachLinkViewModel @Inject constructor(
    private val repository: CoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachLinkUiState())
    val uiState: StateFlow<CoachLinkUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CoachLinkEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: CoachLinkEvent) {
        when (event) {
            is CoachLinkEvent.InviteCodeChanged -> _uiState.update {
                it.copy(inviteCode = event.code.uppercase().take(6), error = null)
            }
            CoachLinkEvent.LinkCoach -> linkCoach()
            is CoachLinkEvent.UnlinkCoach -> unlinkCoach(event.connectionId)
        }
    }

    private fun load() {
        viewModelScope.launch {
            repository.getMyCoaches()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { connections ->
                    _uiState.update { it.copy(connections = connections, isLoading = false) }
                }
        }
    }

    private fun linkCoach() {
        val code = _uiState.value.inviteCode.trim()
        if (code.length != 6) {
            _uiState.update { it.copy(error = "Enter the 6-character code from your coach") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLinking = true, error = null) }
            repository.linkByCode(code).onSuccess {
                _uiState.update { it.copy(isLinking = false, inviteCode = "") }
                _effects.send(CoachLinkEffect.CoachLinked)
                load()
            }.onFailure { e ->
                _uiState.update { it.copy(isLinking = false, error = e.message) }
                _effects.send(CoachLinkEffect.ShowError(e.message ?: "Link failed"))
            }
        }
    }

    private fun unlinkCoach(connectionId: String) {
        viewModelScope.launch {
            repository.unlinkCoach(connectionId).onSuccess { load() }
                .onFailure { e -> _effects.send(CoachLinkEffect.ShowError(e.message ?: "Unlink failed")) }
        }
    }
}
