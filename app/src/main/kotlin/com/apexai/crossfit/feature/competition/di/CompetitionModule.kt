package com.apexai.crossfit.feature.competition.di

import com.apexai.crossfit.feature.competition.data.CompetitionRepositoryImpl
import com.apexai.crossfit.feature.competition.domain.CompetitionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CompetitionModule {

    @Binds
    @Singleton
    abstract fun bindCompetitionRepository(
        impl: CompetitionRepositoryImpl
    ): CompetitionRepository
}
