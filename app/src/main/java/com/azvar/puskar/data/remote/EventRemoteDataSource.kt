package com.azvar.puskar.data.remote

import com.azvar.puskar.data.local.EventEntity
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

interface EventRemoteDataSource {
    suspend fun fetchEvents(): List<EventDto>
}

@Serializable
data class EventDto(
    val id: String,
    val title: String,
    val organizer: String,
    val category: String,
    val description: String,
    /** ISO-8601 local date-time in WIB, e.g. 2026-11-20T09:00 */
    val startAt: String,
    val endAt: String,
    val location: String,
    val isOnline: Boolean,
    val registrationUrl: String,
)

fun EventDto.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    organizer = organizer,
    category = category,
    description = description,
    // Normalise so text ordering in SQL matches chronological ordering.
    startAt = LocalDateTime.parse(startAt).toString(),
    endAt = LocalDateTime.parse(endAt).toString(),
    location = location,
    isOnline = isOnline,
    registrationUrl = registrationUrl,
)
