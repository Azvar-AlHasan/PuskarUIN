package com.azvar.puskar.data.remote.demo

import android.content.Context
import com.azvar.puskar.data.remote.EventDto
import com.azvar.puskar.data.remote.EventRemoteDataSource
import com.azvar.puskar.data.remote.IoDispatcher
import com.azvar.puskar.data.remote.JobDto
import com.azvar.puskar.data.remote.JobRemoteDataSource
import com.azvar.puskar.domain.model.nowInEventZone
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/*
 * Demo dataset bundled in assets/.
 *
 * Dates are stored relative to "today" (e.g. deadlineInDays = 12) and resolved when the data
 * is loaded, so the demo always shows a realistic mix of open, closing-soon and closed
 * openings no matter when the app is installed.
 */

private val demoJson = Json { ignoreUnknownKeys = true }

@Serializable
internal data class DemoJob(
    val id: String,
    val title: String,
    val company: String,
    val industry: String,
    val companyAbout: String,
    val location: String,
    val type: String,
    val workMode: String,
    val description: String,
    val requirements: List<String>,
    val benefits: List<String>,
    val salary: String? = null,
    val postedDaysAgo: Long,
    val deadlineInDays: Long,
    val applyUrl: String,
) {
    fun toDto(today: LocalDate) = JobDto(
        id = id,
        title = title,
        company = company,
        industry = industry,
        companyAbout = companyAbout,
        location = location,
        type = type,
        workMode = workMode,
        description = description,
        requirements = requirements,
        benefits = benefits,
        salary = salary,
        postedAt = today.minusDays(postedDaysAgo).toString(),
        deadline = today.plusDays(deadlineInDays).toString(),
        applyUrl = applyUrl,
    )
}

@Serializable
internal data class DemoEvent(
    val id: String,
    val title: String,
    val organizer: String,
    val category: String,
    val description: String,
    val startInDays: Long,
    /** Local WIB time, e.g. "09:00" */
    val start: String,
    val end: String,
    val location: String,
    val isOnline: Boolean,
    val registrationUrl: String,
) {
    fun toDto(today: LocalDate): EventDto {
        val date = today.plusDays(startInDays)
        return EventDto(
            id = id,
            title = title,
            organizer = organizer,
            category = category,
            description = description,
            startAt = date.atTime(LocalTime.parse(start)).toString(),
            endAt = date.atTime(LocalTime.parse(end)).toString(),
            location = location,
            isOnline = isOnline,
            registrationUrl = registrationUrl,
        )
    }
}

class DemoJobDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock,
) : JobRemoteDataSource {
    override suspend fun fetchJobs(): List<JobDto> = withContext(ioDispatcher) {
        val today = clock.nowInEventZone().toLocalDate()
        context.readAsset<List<DemoJob>>("jobs.json").map { it.toDto(today) }
    }
}

class DemoEventDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock,
) : EventRemoteDataSource {
    override suspend fun fetchEvents(): List<EventDto> = withContext(ioDispatcher) {
        val today = clock.nowInEventZone().toLocalDate()
        context.readAsset<List<DemoEvent>>("events.json").map { it.toDto(today) }
    }
}

private inline fun <reified T> Context.readAsset(name: String): T =
    demoJson.decodeFromString(assets.open(name).bufferedReader().use { it.readText() })
