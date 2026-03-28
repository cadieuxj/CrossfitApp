package com.apexai.crossfit.feature.nutrition.di

import com.apexai.crossfit.feature.nutrition.data.NutritionRepositoryImpl
import com.apexai.crossfit.feature.nutrition.domain.NutritionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NutritionModule {

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        impl: NutritionRepositoryImpl
    ): NutritionRepository
}
