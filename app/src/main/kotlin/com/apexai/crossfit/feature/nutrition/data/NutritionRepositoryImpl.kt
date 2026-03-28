package com.apexai.crossfit.feature.nutrition.data

import com.apexai.crossfit.core.domain.model.CommonFood
import com.apexai.crossfit.core.domain.model.DailyMacroSummary
import com.apexai.crossfit.core.domain.model.MacroEntry
import com.apexai.crossfit.core.domain.model.MacroEntryInput
import com.apexai.crossfit.core.domain.model.MacroTargets
import com.apexai.crossfit.core.domain.model.MealType
import com.apexai.crossfit.feature.nutrition.domain.NutritionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class MacroTargetsRow(
    val user_id: String,
    val calories_kcal: Int,
    val protein_g: Int,
    val carbs_g: Int,
    val fat_g: Int,
    val rest_day_calories: Int? = null,
    val rest_day_protein_g: Int? = null,
    val rest_day_carbs_g: Int? = null,
    val rest_day_fat_g: Int? = null
)

@Serializable
data class MacroEntryRow(
    val id: String,
    val user_id: String,
    val logged_date: String,
    val meal_type: String,
    val food_name: String,
    val calories: Int,
    val protein_g: Double,
    val carbs_g: Double,
    val fat_g: Double,
    val notes: String? = null,
    val logged_at: String
)

@Serializable
data class CommonFoodRow(
    val id: Int,
    val name: String,
    val serving_g: Int,
    val calories: Int,
    val protein_g: Double,
    val carbs_g: Double,
    val fat_g: Double,
    val category: String? = null
)

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : NutritionRepository {

    override fun getTargets(): Flow<MacroTargets?> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: run { emit(null); return@flow }
        val rows = supabase.postgrest["macro_targets"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<MacroTargetsRow>()
        emit(rows.firstOrNull()?.toDomain())
    }

    override suspend fun saveTargets(targets: MacroTargets): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not authenticated"))
            supabase.postgrest["macro_targets"]
                .upsert(
                    mapOf(
                        "user_id"            to userId,
                        "calories_kcal"      to targets.caloriesKcal,
                        "protein_g"          to targets.proteinG,
                        "carbs_g"            to targets.carbsG,
                        "fat_g"              to targets.fatG,
                        "rest_day_calories"  to targets.restDayCalories,
                        "rest_day_protein_g" to targets.restDayProteinG,
                        "rest_day_carbs_g"   to targets.restDayCarbsG,
                        "rest_day_fat_g"     to targets.restDayFatG
                    ),
                    onConflict = "user_id"
                )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailySummary(date: LocalDate): Flow<DailyMacroSummary> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@flow
        val rows = supabase.postgrest["macro_entries"]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("logged_date", date.toString())
                }
            }
            .decodeList<MacroEntryRow>()
        val entries = rows.map { it.toDomain() }
        emit(
            DailyMacroSummary(
                date          = date,
                totalCalories = entries.sumOf { it.calories },
                totalProteinG = entries.sumOf { it.proteinG },
                totalCarbsG   = entries.sumOf { it.carbsG },
                totalFatG     = entries.sumOf { it.fatG },
                entries       = entries
            )
        )
    }

    override suspend fun logEntry(input: MacroEntryInput): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not authenticated"))
            supabase.postgrest["macro_entries"]
                .insert(
                    mapOf(
                        "user_id"     to userId,
                        "logged_date" to input.loggedDate.toString(),
                        "meal_type"   to input.mealType.name,
                        "food_name"   to input.foodName,
                        "calories"    to input.calories,
                        "protein_g"   to input.proteinG,
                        "carbs_g"     to input.carbsG,
                        "fat_g"       to input.fatG,
                        "notes"       to input.notes
                    )
                )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEntry(entryId: String): Result<Unit> {
        return try {
            supabase.postgrest["macro_entries"]
                .delete { filter { eq("id", entryId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchCommonFoods(query: String): Flow<List<CommonFood>> = flow {
        if (query.isBlank()) { emit(emptyList()); return@flow }
        val rows = supabase.postgrest["common_foods"]
            .select {
                filter { ilike("name", "%$query%") }
                limit(10)
            }
            .decodeList<CommonFoodRow>()
        emit(rows.map { it.toDomain() })
    }

    private fun MacroTargetsRow.toDomain() = MacroTargets(
        userId       = user_id,
        caloriesKcal = calories_kcal,
        proteinG     = protein_g,
        carbsG       = carbs_g,
        fatG         = fat_g,
        restDayCalories  = rest_day_calories,
        restDayProteinG  = rest_day_protein_g,
        restDayCarbsG    = rest_day_carbs_g,
        restDayFatG      = rest_day_fat_g
    )

    private fun MacroEntryRow.toDomain() = MacroEntry(
        id         = id,
        userId     = user_id,
        loggedDate = LocalDate.parse(logged_date),
        mealType   = MealType.valueOf(meal_type),
        foodName   = food_name,
        calories   = calories,
        proteinG   = protein_g,
        carbsG     = carbs_g,
        fatG       = fat_g,
        notes      = notes,
        loggedAt   = Instant.parse(logged_at)
    )

    private fun CommonFoodRow.toDomain() = CommonFood(
        id        = id,
        name      = name,
        servingG  = serving_g,
        calories  = calories,
        proteinG  = protein_g,
        carbsG    = carbs_g,
        fatG      = fat_g,
        category  = category
    )
}
