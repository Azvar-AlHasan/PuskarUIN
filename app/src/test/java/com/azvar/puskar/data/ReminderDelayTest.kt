package com.azvar.puskar.data

import com.azvar.puskar.data.reminder.WorkManagerEventReminderScheduler.Companion.reminderDelay
import com.azvar.puskar.testing.FIXED_NOW
import com.azvar.puskar.testing.fixedClock
import com.azvar.puskar.testing.testEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.ZoneId

class ReminderDelayTest {

    @Test
    fun `fires one hour before the event starts`() {
        val event = testEvent("e", FIXED_NOW.plusHours(5))
        assertEquals(Duration.ofHours(4), reminderDelay(event, fixedClock))
    }

    @Test
    fun `fires immediately when the event starts in less than an hour`() {
        val event = testEvent("e", FIXED_NOW.plusMinutes(30))
        assertEquals(Duration.ZERO, reminderDelay(event, fixedClock))
    }

    @Test
    fun `uses WIB even when the phone is set to another time zone`() {
        // Same instant, but the device thinks it is in Tokyo (WIB + 2h).
        val tokyoClock = Clock.fixed(fixedClock.instant(), ZoneId.of("Asia/Tokyo"))
        val event = testEvent("e", FIXED_NOW.plusHours(5))
        assertEquals(Duration.ofHours(4), reminderDelay(event, tokyoClock))
    }
}
