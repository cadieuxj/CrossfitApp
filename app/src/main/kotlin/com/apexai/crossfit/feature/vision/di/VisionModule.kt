package com.apexai.crossfit.feature.vision.di

import com.apexai.crossfit.feature.vision.data.CoachingRepositoryImpl
import com.apexai.crossfit.feature.vision.domain.CoachingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VisionModule {

    @Binds
    @Singleton
    abstract fun bindCoachingRepository(
        impl: CoachingRepositoryImpl
    ): CoachingRepository
}
