package com.apexai.crossfit.feature.wod.di

import com.apexai.crossfit.feature.wod.data.WodRepositoryImpl
import com.apexai.crossfit.feature.wod.domain.WodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WodModule {

    @Binds
    @Singleton
    abstract fun bindWodRepository(
        impl: WodRepositoryImpl
    ): WodRepository
}
