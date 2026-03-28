package com.apexai.crossfit.feature.auth.domain.usecase

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("Password cannot be empty"))
        return repository.login(email.trim(), password)
    }
}
