package com.apexai.crossfit.feature.nutrition.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacroReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val reminderTime = LocalTime.of(20, 0) // 8 PM local

    fun schedule() {
        val now = LocalDateTime.now()
        val nextRun = now.toLocalDate().atTime(reminderTime).let { target ->
            if (now.toLocalTime().isBefore(reminderTime)) target else target.plusDays(1)
        }
        val initialDelay = Duration.between(now, nextRun).toMinutes()

        val request = PeriodicWorkRequestBuilder<MacroReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MacroReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(MacroReminderWorker.WORK_NAME)
    }
}
