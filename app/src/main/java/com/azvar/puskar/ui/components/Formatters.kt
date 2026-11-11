package com.azvar.puskar.ui.components

import com.azvar.puskar.domain.model.CareerEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val ID = Locale.forLanguageTag("id-ID")
private val DAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID)
private val SHORT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", ID)
private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm", ID)

/** "Kamis, 20 November 2026" */
fun CareerEvent.formattedDate(): String = startAt.format(DAY_DATE)

/** "Kam, 20 Nov · 09.00–15.00 WIB" (or with the end date if it spans several days) */
fun CareerEvent.formattedShortSchedule(): String {
    val sameDay = startAt.toLocalDate() == endAt.toLocalDate()
    val end = if (sameDay) endAt.format(TIME) else "${endAt.format(SHORT_DATE)} ${endAt.format(TIME)}"
    return "${startAt.format(SHORT_DATE)} · ${startAt.format(TIME)}–$end WIB"
}

/** "09.00–15.00 WIB" */
fun CareerEvent.formattedTimeRange(): String = "${startAt.format(TIME)}–${endAt.format(TIME)} WIB"

/** "Hari ini", "Kemarin", "3 hari lalu", "2 minggu lalu" */
fun relativePostedLabel(postedAt: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(postedAt, today)
    return when {
        days <= 0 -> "Hari ini"
        days == 1L -> "Kemarin"
        days < 7 -> "$days hari lalu"
        days < 30 -> "${days / 7} minggu lalu"
        else -> "${days / 30} bulan lalu"
    }
}

/** "Ditutup", "Tutup hari ini", "3 hari lagi" */
fun deadlineLabel(deadline: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(today, deadline)
    return when {
        days < 0 -> "Ditutup"
        days == 0L -> "Tutup hari ini"
        else -> "$days hari lagi"
    }
}

fun LocalDate.formattedLong(): String = format(DateTimeFormatter.ofPattern("d MMMM yyyy", ID))
