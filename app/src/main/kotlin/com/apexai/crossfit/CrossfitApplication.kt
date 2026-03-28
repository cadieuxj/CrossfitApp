package com.apexai.crossfit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.apexai.crossfit.feature.nutrition.worker.MacroReminderScheduler
import com.apexai.crossfit.feature.nutrition.worker.MacroReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CrossfitApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var macroReminderScheduler: MacroReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        macroReminderScheduler.schedule()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MacroReminderWorker.CHANNEL_ID,
                "Nutrition Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Evening reminders to log remaining meals toward your calorie goal"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
