package com.apexai.crossfit.feature.readiness.di

import com.apexai.crossfit.feature.readiness.data.ReadinessRepositoryImpl
import com.apexai.crossfit.feature.readiness.domain.ReadinessRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReadinessModule {

    @Binds
    @Singleton
    abstract fun bindReadinessRepository(
        impl: ReadinessRepositoryImpl
    ): ReadinessRepository
}
