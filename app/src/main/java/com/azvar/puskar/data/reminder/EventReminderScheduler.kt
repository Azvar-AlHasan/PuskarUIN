package com.azvar.puskar.data.reminder

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.EVENT_ZONE
import java.time.Clock
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Schedules a local notification shortly before an event starts. */
interface EventReminderScheduler {
    fun schedule(event: CareerEvent)
    fun cancel(eventId: String)
}

/**
 * WorkManager-backed scheduler. WorkManager survives app restarts and device reboots,
 * and does not need the exact-alarm permission, which is overkill for a reminder.
 */
class WorkManagerEventReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val clock: Clock,
) : EventReminderScheduler {

    override fun schedule(event: CareerEvent) {
        val delay = reminderDelay(event, clock)
        val request = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(EventReminderWorker.KEY_EVENT_ID, event.id)
                    .putString(EventReminderWorker.KEY_TITLE, event.title)
                    .putString(EventReminderWorker.KEY_START, event.startAt.toString())
                    .putString(EventReminderWorker.KEY_LOCATION, event.location)
                    .build(),
            )
            .addTag(TAG)
            .build()
        workManager.enqueueUniqueWork(workName(event.id), ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(eventId: String) {
        workManager.cancelUniqueWork(workName(eventId))
    }

    companion object {
        const val TAG = "event-reminder"
        val LEAD_TIME: Duration = Duration.ofHours(1)

        fun workName(eventId: String) = "event-reminder-$eventId"

        /** Time until the reminder should fire; never negative, so a late toggle fires right away. */
        fun reminderDelay(event: CareerEvent, clock: Clock): Duration {
            val fireAt = event.startAt.atZone(EVENT_ZONE).minus(LEAD_TIME).toInstant()
            val delay = Duration.between(clock.instant(), fireAt)
            return if (delay.isNegative) Duration.ZERO else delay
        }
    }
}
