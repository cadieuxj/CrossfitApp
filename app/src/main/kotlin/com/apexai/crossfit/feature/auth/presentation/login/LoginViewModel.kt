package com.apexai.crossfit.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.feature.auth.domain.usecase.LoginUseCase
import com.apexai.crossfit.feature.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val privacyPolicyAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
)

sealed interface AuthEvent {
    data class EmailChanged(val email: String)             : AuthEvent
    data class PasswordChanged(val password: String)       : AuthEvent
    data class DisplayNameChanged(val name: String)        : AuthEvent
    data class PrivacyPolicyToggled(val accepted: Boolean) : AuthEvent
    data object LoginClicked                               : AuthEvent
    data object RegisterClicked                            : AuthEvent
}

sealed interface AuthEffect {
    data object NavigateToHome  : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged          -> _uiState.update { it.copy(email = event.email, emailError = null) }
            is AuthEvent.PasswordChanged       -> _uiState.update { it.copy(password = event.password, passwordError = null) }
            is AuthEvent.DisplayNameChanged    -> _uiState.update { it.copy(displayName = event.name) }
            is AuthEvent.PrivacyPolicyToggled  -> _uiState.update { it.copy(privacyPolicyAccepted = event.accepted) }
            is AuthEvent.LoginClicked          -> login()
            is AuthEvent.RegisterClicked       -> register()
        }
    }

    private fun login() {
        val state = _uiState.value
        if (!validateLogin(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            loginUseCase(state.email, state.password)
                .onSuccess { _effects.send(AuthEffect.NavigateToHome) }
                .onFailure { error ->
                    _uiState.update { it.copy(generalError = error.message) }
                    _effects.send(AuthEffect.ShowError(error.message ?: "Login failed"))
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun register() {
        val state = _uiState.value
        if (!validateRegister(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            registerUseCase(state.email, state.password, state.displayName)
                .onSuccess { _effects.send(AuthEffect.NavigateToHome) }
                .onFailure { error ->
                    _uiState.update { it.copy(generalError = error.message) }
                    _effects.send(AuthEffect.ShowError(error.message ?: "Registration failed"))
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun validateLogin(state: AuthUiState): Boolean {
        var valid = true
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            valid = false
        }
        if (state.password.length < 12) {
            _uiState.update { it.copy(passwordError = "Password must be at least 12 characters") }
            valid = false
        }
        return valid
    }

    private fun validateRegister(state: AuthUiState): Boolean {
        var valid = validateLogin(state)
        if (state.displayName.isBlank()) {
            _uiState.update { it.copy(generalError = "Display name is required") }
            valid = false
        }
        if (!state.privacyPolicyAccepted) {
            _uiState.update { it.copy(generalError = "You must accept the Privacy Policy to create an account") }
            valid = false
        }
        return valid
    }
}
