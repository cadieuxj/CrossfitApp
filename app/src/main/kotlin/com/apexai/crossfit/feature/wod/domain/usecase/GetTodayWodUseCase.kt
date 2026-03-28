package com.apexai.crossfit.feature.wod.domain.usecase

import com.apexai.crossfit.core.domain.model.Workout
import com.apexai.crossfit.feature.wod.domain.WodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayWodUseCase @Inject constructor(
    private val repository: WodRepository
) {
    operator fun invoke(): Flow<Workout?> = repository.getTodayWorkout()
}
