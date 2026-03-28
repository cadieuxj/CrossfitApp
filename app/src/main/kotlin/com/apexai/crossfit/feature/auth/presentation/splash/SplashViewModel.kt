package com.apexai.crossfit.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashEffect {
    data object NavigateToHome : SplashEffect
    data object NavigateToLogin : SplashEffect
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                if (session != null) {
                    _effect.send(SplashEffect.NavigateToHome)
                } else {
                    _effect.send(SplashEffect.NavigateToLogin)
                }
            }
        }
    }
}
