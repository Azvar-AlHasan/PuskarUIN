package com.azvar.puskar.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.EventCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organizer: String,
    val category: String,
    val description: String,
    /** ISO-8601 local date-time; sorts correctly as text. */
    @ColumnInfo(name = "start_at") val startAt: String,
    @ColumnInfo(name = "end_at") val endAt: String,
    val location: String,
    @ColumnInfo(name = "is_online") val isOnline: Boolean,
    @ColumnInfo(name = "registration_url") val registrationUrl: String,
)

/** Kept separate from [EventEntity] for the same reason as bookmarks: refreshes must not wipe user state. */
@Entity(tableName = "event_reminders")
data class EventReminderEntity(
    @PrimaryKey @ColumnInfo(name = "event_id") val eventId: String,
)

data class EventWithReminder(
    @Embedded val event: EventEntity,
    @ColumnInfo(name = "is_reminder_set") val isReminderSet: Boolean,
)

fun EventWithReminder.toDomain(): CareerEvent = CareerEvent(
    id = event.id,
    title = event.title,
    organizer = event.organizer,
    category = EventCategory.valueOf(event.category),
    description = event.description,
    startAt = LocalDateTime.parse(event.startAt),
    endAt = LocalDateTime.parse(event.endAt),
    location = event.location,
    isOnline = event.isOnline,
    registrationUrl = event.registrationUrl,
    isReminderSet = isReminderSet,
)

@Dao
interface EventDao {

    @Query(
        """
        SELECT events.*, (event_reminders.event_id IS NOT NULL) AS is_reminder_set
        FROM events
        LEFT JOIN event_reminders ON event_reminders.event_id = events.id
        ORDER BY events.start_at ASC
        """,
    )
    fun observeEvents(): Flow<List<EventWithReminder>>

    @Query(
        """
        SELECT events.*, (event_reminders.event_id IS NOT NULL) AS is_reminder_set
        FROM events
        LEFT JOIN event_reminders ON event_reminders.event_id = events.id
        WHERE events.id = :id
        """,
    )
    fun observeEvent(id: String): Flow<EventWithReminder?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEvent(id: String): EventEntity?

    @Upsert
    suspend fun upsertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id NOT IN (:keepIds)")
    suspend fun deleteEventsNotIn(keepIds: List<String>)

    @Transaction
    suspend fun replaceEvents(events: List<EventEntity>) {
        upsertEvents(events)
        deleteEventsNotIn(events.map { it.id })
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminder(reminder: EventReminderEntity)

    @Query("DELETE FROM event_reminders WHERE event_id = :eventId")
    suspend fun deleteReminder(eventId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM event_reminders WHERE event_id = :eventId)")
    suspend fun isReminderSet(eventId: String): Boolean
}
