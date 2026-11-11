package com.azvar.puskar.data.repository

import com.azvar.puskar.data.local.BookmarkEntity
import com.azvar.puskar.data.local.JobDao
import com.azvar.puskar.data.local.toDomain
import com.azvar.puskar.data.remote.JobRemoteDataSource
import com.azvar.puskar.data.remote.toEntity
import com.azvar.puskar.domain.model.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface JobRepository {
    /** Emits the cached jobs and re-emits whenever the cache or bookmarks change. */
    fun observeJobs(): Flow<List<Job>>

    fun observeJob(id: String): Flow<Job?>

    /** Pulls fresh data from the remote source into the local cache. */
    suspend fun refresh(): Result<Unit>

    suspend fun toggleBookmark(jobId: String)
}

/**
 * Offline-first: the UI only ever reads from Room. The network just keeps Room fresh,
 * so the app still shows the last known list with no connection.
 */
class OfflineFirstJobRepository @Inject constructor(
    private val jobDao: JobDao,
    private val remote: JobRemoteDataSource,
) : JobRepository {

    override fun observeJobs(): Flow<List<Job>> =
        jobDao.observeJobs().map { rows -> rows.map { it.toDomain() } }

    override fun observeJob(id: String): Flow<Job?> =
        jobDao.observeJob(id).map { it?.toDomain() }

    override suspend fun refresh(): Result<Unit> = try {
        val fresh = remote.fetchJobs().map { it.toEntity() }
        jobDao.replaceJobs(fresh)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e // never swallow coroutine cancellation
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleBookmark(jobId: String) {
        if (jobDao.isBookmarked(jobId)) {
            jobDao.deleteBookmark(jobId)
        } else {
            jobDao.insertBookmark(BookmarkEntity(jobId))
        }
    }
}
