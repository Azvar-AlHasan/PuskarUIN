package com.azvar.puskar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    @Query(
        """
        SELECT jobs.*, (bookmarks.job_id IS NOT NULL) AS is_bookmarked
        FROM jobs
        LEFT JOIN bookmarks ON bookmarks.job_id = jobs.id
        ORDER BY jobs.deadline_epoch_day ASC
        """,
    )
    fun observeJobs(): Flow<List<JobWithBookmark>>

    @Query(
        """
        SELECT jobs.*, (bookmarks.job_id IS NOT NULL) AS is_bookmarked
        FROM jobs
        LEFT JOIN bookmarks ON bookmarks.job_id = jobs.id
        WHERE jobs.id = :id
        """,
    )
    fun observeJob(id: String): Flow<JobWithBookmark?>

    @Upsert
    suspend fun upsertJobs(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id NOT IN (:keepIds)")
    suspend fun deleteJobsNotIn(keepIds: List<String>)

    /** Replace the cached list atomically so the UI never sees a half-updated state. */
    @Transaction
    suspend fun replaceJobs(jobs: List<JobEntity>) {
        upsertJobs(jobs)
        deleteJobsNotIn(jobs.map { it.id })
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE job_id = :jobId")
    suspend fun deleteBookmark(jobId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE job_id = :jobId)")
    suspend fun isBookmarked(jobId: String): Boolean
}
