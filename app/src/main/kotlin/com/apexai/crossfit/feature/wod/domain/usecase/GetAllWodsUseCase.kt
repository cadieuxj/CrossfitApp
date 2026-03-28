package com.apexai.crossfit.feature.wod.domain.usecase

import com.apexai.crossfit.core.domain.model.WorkoutSummary
import com.apexai.crossfit.feature.wod.domain.WodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllWodsUseCase @Inject constructor(
    private val repository: WodRepository
) {
    operator fun invoke(): Flow<Result<List<WorkoutSummary>>> = repository.getAllWorkouts()
}
