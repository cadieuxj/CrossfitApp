package com.apexai.crossfit.feature.pr.di

import com.apexai.crossfit.feature.pr.data.PrRepositoryImpl
import com.apexai.crossfit.feature.pr.domain.PrRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrModule {

    @Binds
    @Singleton
    abstract fun bindPrRepository(
        impl: PrRepositoryImpl
    ): PrRepository
}
