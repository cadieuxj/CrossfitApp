package com.apexai.crossfit.core.di

import android.content.Context
import com.apexai.crossfit.core.data.SupabaseClientProvider
import com.apexai.crossfit.core.data.network.FastApiService
import com.apexai.crossfit.core.media.PlayerPoolManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideKtorClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = if (com.apexai.crossfit.BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
        }
        engine {
            connectTimeout = 30_000
            socketTimeout  = 60_000
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(provider: SupabaseClientProvider): SupabaseClient =
        provider.client

    @Provides
    @Singleton
    fun providePlayerPoolManager(
        @ApplicationContext context: Context
    ): PlayerPoolManager = PlayerPoolManager(context)

    @Provides
    @Singleton
    fun provideFastApiService(httpClient: HttpClient): FastApiService =
        FastApiService(httpClient)
}
