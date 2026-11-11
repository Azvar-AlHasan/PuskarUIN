package com.azvar.puskar.domain.model

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

/** Event times are published in Western Indonesia Time, regardless of the device's time zone. */
val EVENT_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

fun Clock.nowInEventZone(): LocalDateTime = LocalDateTime.ofInstant(instant(), EVENT_ZONE)

/** A career event: job fair, workshop, webinar, or company visit. */
data class CareerEvent(
    val id: String,
    val title: String,
    val organizer: String,
    val category: EventCategory,
    val description: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val location: String,
    val isOnline: Boolean,
    val registrationUrl: String,
    val isReminderSet: Boolean = false,
) {
    fun isPast(now: LocalDateTime): Boolean = endAt.isBefore(now)
}

enum class EventCategory(val label: String) {
    JOB_FAIR("Job Fair"),
    WORKSHOP("Workshop"),
    WEBINAR("Webinar"),
    COMPANY_VISIT("Company Visit"),
}
