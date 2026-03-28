package com.apexai.crossfit.feature.nutrition.domain

import com.apexai.crossfit.core.domain.model.CommonFood
import com.apexai.crossfit.core.domain.model.DailyMacroSummary
import com.apexai.crossfit.core.domain.model.MacroEntryInput
import com.apexai.crossfit.core.domain.model.MacroTargets
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface NutritionRepository {
    fun getTargets(): Flow<MacroTargets?>
    suspend fun saveTargets(targets: MacroTargets): Result<Unit>
    fun getDailySummary(date: LocalDate): Flow<DailyMacroSummary>
    suspend fun logEntry(input: MacroEntryInput): Result<Unit>
    suspend fun deleteEntry(entryId: String): Result<Unit>
    fun searchCommonFoods(query: String): Flow<List<CommonFood>>
}
