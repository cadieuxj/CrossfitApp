package com.apexai.crossfit.feature.coach.di

import com.apexai.crossfit.feature.coach.data.CoachRepositoryImpl
import com.apexai.crossfit.feature.coach.domain.CoachRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoachModule {

    @Binds
    @Singleton
    abstract fun bindCoachRepository(
        impl: CoachRepositoryImpl
    ): CoachRepository
}
