package com.azvar.puskar.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.EVENT_ZONE
import com.azvar.puskar.domain.model.Job

/** Opens the system calendar with the event pre-filled. No calendar permission needed. */
fun Context.addToCalendar(event: CareerEvent): Boolean {
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, event.title)
        .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
        .putExtra(CalendarContract.Events.DESCRIPTION, "${event.description}\n\nDaftar: ${event.registrationUrl}")
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startAt.atZone(EVENT_ZONE).toInstant().toEpochMilli())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endAt.atZone(EVENT_ZONE).toInstant().toEpochMilli())
    return startSafely(intent)
}

fun Context.shareEvent(event: CareerEvent): Boolean = shareText(
    "${event.title}\n${event.organizer}\n${event.formattedDate()}, ${event.formattedTimeRange()}\n" +
        "${event.location}\n\nDaftar: ${event.registrationUrl}\n\nInfo dari PuskarUIN",
)

fun Context.shareJob(job: Job): Boolean = shareText(
    "Lowongan ${job.type.label}: ${job.title}\n${job.company} · ${job.location}\n\n" +
        "Lamar: ${job.applyUrl}\n\nInfo dari PuskarUIN",
)

private fun Context.shareText(text: String): Boolean {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    return startSafely(Intent.createChooser(send, null))
}

private fun Context.startSafely(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}
