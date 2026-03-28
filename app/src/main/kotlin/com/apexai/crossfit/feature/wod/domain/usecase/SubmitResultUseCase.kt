package com.apexai.crossfit.feature.wod.domain.usecase

import com.apexai.crossfit.core.domain.model.WorkoutResult
import com.apexai.crossfit.core.domain.model.WorkoutResultInput
import com.apexai.crossfit.feature.wod.domain.WodRepository
import javax.inject.Inject

class SubmitResultUseCase @Inject constructor(
    private val repository: WodRepository
) {
    suspend operator fun invoke(input: WorkoutResultInput): Result<WorkoutResult> {
        if (input.score.isBlank()) return Result.failure(IllegalArgumentException("Score cannot be empty"))
        return repository.logResult(input)
    }
}
