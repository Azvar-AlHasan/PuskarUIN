package com.azvar.puskar.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.azvar.puskar.MainActivity
import com.azvar.puskar.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val start = inputData.getString(KEY_START)?.let(LocalDateTime::parse)
        val location = inputData.getString(KEY_LOCATION).orEmpty()

        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        // Without permission there is nothing useful to do; succeed so WorkManager does not retry.
        if (!canNotify) return Result.success()

        val openApp = PendingIntent.getActivity(
            applicationContext,
            eventId.hashCode(),
            Intent(applicationContext, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_EVENT_ID, eventId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val body = buildString {
            append("Mulai ")
            append(start?.format(TIME_FORMAT) ?: "sebentar lagi")
            if (location.isNotBlank()) append(" · ").append(location)
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(eventId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the call.
        }
        return Result.success()
    }

    companion object {
        const val KEY_EVENT_ID = "event_id"
        const val KEY_TITLE = "title"
        const val KEY_START = "start"
        const val KEY_LOCATION = "location"
        const val CHANNEL_ID = "event_reminders"
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm 'WIB'")

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat event",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Pengingat 1 jam sebelum event karier dimulai" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
