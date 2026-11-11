package com.azvar.puskar.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azvar.puskar.data.local.PuskarDatabase
import com.azvar.puskar.data.reminder.EventReminderScheduler
import com.azvar.puskar.data.remote.EventDto
import com.azvar.puskar.data.remote.EventRemoteDataSource
import com.azvar.puskar.data.repository.OfflineFirstEventRepository
import com.azvar.puskar.domain.model.CareerEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs the real Room queries against an in-memory database. */
@RunWith(AndroidJUnit4::class)
class OfflineFirstEventRepositoryTest {

    private lateinit var db: PuskarDatabase
    private val remote = FakeRemote()
    private val scheduler = RecordingScheduler()
    private lateinit var repository: OfflineFirstEventRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PuskarDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OfflineFirstEventRepository(db.eventDao(), remote, scheduler)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `refresh stores events sorted by start time`() = runTest {
        remote.events = listOf(dto("b", "2026-12-01T09:00"), dto("a", "2026-11-20T09:00"))
        repository.refresh()
        assertEquals(listOf("a", "b"), repository.observeEvents().first().map { it.id })
    }

    @Test
    fun `toggling a reminder schedules and then cancels the notification`() = runTest {
        remote.events = listOf(dto("a", "2026-11-20T09:00"))
        repository.refresh()

        assertTrue(repository.toggleReminder("a"))
        assertEquals(listOf("a"), scheduler.scheduled.map { it.id })
        assertTrue(repository.observeEvent("a").first()!!.isReminderSet)

        assertFalse(repository.toggleReminder("a"))
        assertEquals(listOf("a"), scheduler.cancelled)
        assertFalse(repository.observeEvent("a").first()!!.isReminderSet)
    }

    @Test
    fun `reminders survive a refresh`() = runTest {
        remote.events = listOf(dto("a", "2026-11-20T09:00"))
        repository.refresh()
        repository.toggleReminder("a")

        remote.events = listOf(dto("a", "2026-11-20T09:00", title = "Updated title"))
        repository.refresh()

        val event = repository.observeEvent("a").first()!!
        assertEquals("Updated title", event.title)
        assertTrue(event.isReminderSet)
    }

    @Test
    fun `events removed upstream disappear locally`() = runTest {
        remote.events = listOf(dto("a", "2026-11-20T09:00"), dto("b", "2026-11-21T09:00"))
        repository.refresh()
        remote.events = listOf(dto("b", "2026-11-21T09:00"))
        repository.refresh()
        assertEquals(listOf("b"), repository.observeEvents().first().map { it.id })
    }

    @Test
    fun `failed refresh keeps the cached events`() = runTest {
        remote.events = listOf(dto("a", "2026-11-20T09:00"))
        repository.refresh()
        remote.fail = true
        assertTrue(repository.refresh().isFailure)
        assertEquals(listOf("a"), repository.observeEvents().first().map { it.id })
    }

    private fun dto(id: String, start: String, title: String = "Event $id") = EventDto(
        id = id,
        title = title,
        organizer = "Org",
        category = "WEBINAR",
        description = "",
        startAt = start,
        endAt = start.replaceRange(11, 13, "23"),
        location = "Online",
        isOnline = true,
        registrationUrl = "https://example.com",
    )

    private class FakeRemote : EventRemoteDataSource {
        var events: List<EventDto> = emptyList()
        var fail = false
        override suspend fun fetchEvents(): List<EventDto> = if (fail) error("offline") else events
    }

    private class RecordingScheduler : EventReminderScheduler {
        val scheduled = mutableListOf<CareerEvent>()
        val cancelled = mutableListOf<String>()
        override fun schedule(event: CareerEvent) {
            scheduled += event
        }
        override fun cancel(eventId: String) {
            cancelled += eventId
        }
    }
}
