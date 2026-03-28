package com.apexai.crossfit.feature.auth.domain.usecase

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthSession> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email is required"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        if (displayName.isBlank()) return Result.failure(IllegalArgumentException("Display name is required"))
        return repository.register(email.trim(), password, displayName.trim())
    }
}
