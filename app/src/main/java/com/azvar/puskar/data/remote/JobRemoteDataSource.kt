package com.azvar.puskar.data.remote

import android.content.Context
import com.azvar.puskar.data.local.JobEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

/**
 * Source of truth for job openings outside the device.
 *
 * Today it is backed by a bundled JSON file ([AssetJobRemoteDataSource]) so the app
 * runs without any setup. The next milestone swaps in a Firestore implementation;
 * because the repository only depends on this interface, nothing else has to change.
 */
interface JobRemoteDataSource {
    suspend fun fetchJobs(): List<JobDto>
}

@Serializable
data class JobDto(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val type: String,
    val workMode: String,
    val description: String,
    val requirements: List<String>,
    /** ISO-8601 date, e.g. 2026-12-15 */
    val deadline: String,
    val applyUrl: String,
)

fun JobDto.toEntity(): JobEntity = JobEntity(
    id = id,
    title = title,
    company = company,
    location = location,
    type = type,
    workMode = workMode,
    description = description,
    requirements = requirements.joinToString("\n"),
    deadlineEpochDay = LocalDate.parse(deadline).toEpochDay(),
    applyUrl = applyUrl,
)

class AssetJobRemoteDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : JobRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchJobs(): List<JobDto> = withContext(ioDispatcher) {
        val raw = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        json.decodeFromString<List<JobDto>>(raw)
    }

    private companion object {
        const val ASSET_FILE = "jobs.json"
    }
}
