package com.apexai.crossfit.feature.auth.data

import com.apexai.crossfit.core.domain.model.AuthSession
import com.apexai.crossfit.core.domain.model.UserProfile
import com.apexai.crossfit.feature.auth.domain.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProfileRow(
    val id: String,
    val display_name: String,
    val avatar_url: String? = null,
    val created_at: String
)

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> =
        runCatching {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val session = supabase.auth.currentSessionOrNull()
                ?: error("No session after login")
            session.toAuthSession()
        }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthSession> = runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = kotlinx.serialization.json.buildJsonObject {
                put("display_name", kotlinx.serialization.json.JsonPrimitive(displayName))
            }
        }
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No session after registration")
        session.toAuthSession()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabase.auth.signOut()
    }

    override fun observeSession(): Flow<AuthSession?> =
        supabase.auth.sessionStatus.map { status ->
            when (status) {
                is io.github.jan.supabase.auth.status.SessionStatus.Authenticated ->
                    status.session.toAuthSession()
                else -> null
            }
        }

    override suspend fun refreshToken(): Result<AuthSession> = runCatching {
        supabase.auth.refreshCurrentSession()
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No session after refresh")
        session.toAuthSession()
    }

    override suspend fun getCurrentProfile(): Result<UserProfile> = runCatching {
        val user = supabase.auth.currentUserOrNull()
            ?: error("No authenticated user")
        val row = supabase.postgrest["profiles"]
            .select { filter { eq("id", user.id) } }
            .decodeSingle<ProfileRow>()
        UserProfile(
            id          = row.id,
            email       = user.email ?: "",
            displayName = row.display_name,
            createdAt   = Instant.parse(row.created_at),
            avatarUrl   = row.avatar_url
        )
    }

    private fun io.github.jan.supabase.auth.user.UserSession.toAuthSession() = AuthSession(
        accessToken  = accessToken,
        refreshToken = refreshToken,
        userId       = user?.id ?: "",
        expiresAt    = expiresAt?.toEpochMilliseconds() ?: 0L
    )
}
