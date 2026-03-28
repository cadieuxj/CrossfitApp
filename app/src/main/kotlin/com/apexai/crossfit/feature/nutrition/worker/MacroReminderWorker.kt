package com.apexai.crossfit.feature.nutrition.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apexai.crossfit.MainActivity
import com.apexai.crossfit.R
import com.apexai.crossfit.feature.nutrition.domain.NutritionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.math.roundToInt

@HiltWorker
class MacroReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val nutritionRepository: NutritionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val targets = nutritionRepository.getTargets().first() ?: return Result.success()
        if (targets.calories <= 0) return Result.success()

        val summary = nutritionRepository.getDailySummary(LocalDate.now()).first()
        val pct = (summary.totalCalories.toFloat() / targets.calories * 100).roundToInt()

        if (pct >= 70) return Result.success() // Goal mostly met — no nudge needed

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("navigate_to", "nutrition/log")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Log your remaining meals")
            .setContentText("You're at $pct% of your calorie goal. Log your remaining meals.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You're at $pct% of your calorie goal. Log your remaining meals.")
            )
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID      = "NUTRITION_REMINDER"
        const val WORK_NAME       = "macro_evening_reminder"
        private const val NOTIFICATION_ID = 1001
    }
}
