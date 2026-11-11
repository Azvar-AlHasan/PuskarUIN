package com.azvar.puskar.data.repository

import com.azvar.puskar.data.local.EventDao
import com.azvar.puskar.data.local.EventReminderEntity
import com.azvar.puskar.data.local.EventWithReminder
import com.azvar.puskar.data.local.toDomain
import com.azvar.puskar.data.reminder.EventReminderScheduler
import com.azvar.puskar.data.remote.EventRemoteDataSource
import com.azvar.puskar.data.remote.toEntity
import com.azvar.puskar.domain.model.CareerEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface EventRepository {
    fun observeEvents(): Flow<List<CareerEvent>>
    fun observeEvent(id: String): Flow<CareerEvent?>
    suspend fun refresh(): Result<Unit>

    /** Turns the reminder on or off. Returns the new state. */
    suspend fun toggleReminder(eventId: String): Boolean
}

class OfflineFirstEventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val remote: EventRemoteDataSource,
    private val reminderScheduler: EventReminderScheduler,
) : EventRepository {

    override fun observeEvents(): Flow<List<CareerEvent>> =
        eventDao.observeEvents().map { rows -> rows.map { it.toDomain() } }

    override fun observeEvent(id: String): Flow<CareerEvent?> =
        eventDao.observeEvent(id).map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> = try {
        eventDao.replaceEvents(remote.fetchEvents().map { it.toEntity() })
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleReminder(eventId: String): Boolean {
        return if (eventDao.isReminderSet(eventId)) {
            eventDao.deleteReminder(eventId)
            reminderScheduler.cancel(eventId)
            false
        } else {
            val entity = eventDao.getEvent(eventId) ?: return false
            eventDao.insertReminder(EventReminderEntity(eventId))
            reminderScheduler.schedule(EventWithReminder(entity, isReminderSet = true).toDomain())
            true
        }
    }
}
