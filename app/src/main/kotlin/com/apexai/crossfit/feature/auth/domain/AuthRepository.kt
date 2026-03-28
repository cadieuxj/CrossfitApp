package com.apexai.crossfit.feature.auth.domain

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    fun observeSession(): Flow<AuthSession?>
    suspend fun refreshToken(): Result<AuthSession>
    suspend fun getCurrentProfile(): Result<UserProfile>
}
