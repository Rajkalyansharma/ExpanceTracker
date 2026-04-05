package com.rajkalyansharma.expancetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rajkalyansharma.expancetracker.util.NotificationHelper

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            applicationContext,
            "Daily Reminder 🔔",
            "Track your daily expense to complete your goal 🎯"
        )
        return Result.success()
    }
}
