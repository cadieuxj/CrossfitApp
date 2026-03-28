package com.apexai.crossfit.feature.nutrition

import android.app.NotificationManager
import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.apexai.crossfit.core.domain.model.CommonFood
import com.apexai.crossfit.core.domain.model.DailyMacroSummary
import com.apexai.crossfit.core.domain.model.MacroEntryInput
import com.apexai.crossfit.core.domain.model.MacroTargets
import com.apexai.crossfit.feature.nutrition.domain.NutritionRepository
import com.apexai.crossfit.feature.nutrition.worker.MacroReminderWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [MacroReminderWorker].
 *
 * Uses pure mockk mocks — no Robolectric required. The NotificationManager
 * is mocked via a stubbed Context so we can verify `notify()` calls without
 * the Android framework.
 */
class MacroReminderWorkerTest {

    // ── Guard: targets null ───────────────────────────────────────────────────

    @Test
    fun `returns success without posting notification when targets are null`() = runTest {
        val (worker, nm) = buildWorker(targets = null, loggedCalories = 0, targetCalories = 0)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 0) { nm.notify(any(), any()) }
    }

    // ── Guard: target calories zero ───────────────────────────────────────────

    @Test
    fun `returns success without posting notification when calorie target is zero`() = runTest {
        val (worker, nm) = buildWorker(targets = macroTargets(0), loggedCalories = 0, targetCalories = 0)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 0) { nm.notify(any(), any()) }
    }

    // ── Guard: pct >= 70 ──────────────────────────────────────────────────────

    @Test
    fun `returns success without posting notification when logged equals 70 percent of target`() = runTest {
        val (worker, nm) = buildWorker(targets = macroTargets(2000), loggedCalories = 1400, targetCalories = 2000)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 0) { nm.notify(any(), any()) }
    }

    @Test
    fun `returns success without posting notification when logged exceeds target`() = runTest {
        val (worker, nm) = buildWorker(targets = macroTargets(2000), loggedCalories = 2500, targetCalories = 2000)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 0) { nm.notify(any(), any()) }
    }

    // ── Notification fires: pct < 70 ──────────────────────────────────────────

    @Test
    fun `posts notification when logged calories are below 70 percent of target`() = runTest {
        val (worker, nm) = buildWorker(targets = macroTargets(2000), loggedCalories = 1000, targetCalories = 2000)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 1) { nm.notify(any(), any()) }
    }

    @Test
    fun `posts notification when no calories have been logged`() = runTest {
        val (worker, nm) = buildWorker(targets = macroTargets(2000), loggedCalories = 0, targetCalories = 2000)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 1) { nm.notify(any(), any()) }
    }

    @Test
    fun `posts notification when one calorie short of 70 percent threshold`() = runTest {
        // 1399 / 2000 = 69.95% → rounds to 69% → below 70
        val (worker, nm) = buildWorker(targets = macroTargets(2000), loggedCalories = 1399, targetCalories = 2000)

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        verify(exactly = 1) { nm.notify(any(), any()) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private data class WorkerAndNm(val worker: MacroReminderWorker, val nm: NotificationManager)

    private fun buildWorker(
        targets: MacroTargets?,
        loggedCalories: Int,
        targetCalories: Int
    ): WorkerAndNm {
        val repo = fakeRepo(targets, loggedCalories)
        val nm = mockk<NotificationManager>(relaxed = true)

        val context = mockk<Context>(relaxed = true) {
            every { getSystemService(Context.NOTIFICATION_SERVICE) } returns nm
            every { applicationContext } returns this
            every { packageName } returns "com.apexai.crossfit"
        }

        val params = mockk<WorkerParameters>(relaxed = true)

        return WorkerAndNm(MacroReminderWorker(context, params, repo), nm)
    }

    private fun fakeRepo(targets: MacroTargets?, loggedCalories: Int): NutritionRepository =
        object : NutritionRepository {
            override fun getTargets() = flowOf(targets)
            override fun getDailySummary(date: LocalDate) = flowOf(
                DailyMacroSummary(
                    date = LocalDate.now(),
                    totalCalories = loggedCalories,
                    totalProtein = 0f, totalCarbs = 0f, totalFat = 0f,
                    entries = emptyList()
                )
            )
            override suspend fun saveTargets(t: MacroTargets) = kotlin.Result.success(Unit)
            override suspend fun logEntry(input: MacroEntryInput) = kotlin.Result.success(Unit)
            override suspend fun deleteEntry(entryId: String) = kotlin.Result.success(Unit)
            override fun searchCommonFoods(query: String) = flowOf(emptyList<CommonFood>())
        }

    private fun macroTargets(calories: Int) = MacroTargets(
        calories = calories, protein = 150f, carbs = 200f, fat = 60f
    )
}
