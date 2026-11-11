package com.azvar.puskar.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.domain.model.WorkMode
import java.time.LocalDate

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val location: String,
    val type: String,
    @ColumnInfo(name = "work_mode") val workMode: String,
    val description: String,
    /** Stored newline-separated to avoid a TypeConverter for a simple list. */
    val requirements: String,
    /** Stored as epoch day so it sorts correctly in SQL. */
    @ColumnInfo(name = "deadline_epoch_day") val deadlineEpochDay: Long,
    @ColumnInfo(name = "apply_url") val applyUrl: String,
)

/**
 * Bookmarks live in their own table so refreshing jobs from the network
 * never wipes what the user saved.
 */
@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey @ColumnInfo(name = "job_id") val jobId: String,
)

data class JobWithBookmark(
    @Embedded val job: JobEntity,
    @ColumnInfo(name = "is_bookmarked") val isBookmarked: Boolean,
)

fun JobWithBookmark.toDomain(): Job = Job(
    id = job.id,
    title = job.title,
    company = job.company,
    location = job.location,
    type = JobType.valueOf(job.type),
    workMode = WorkMode.valueOf(job.workMode),
    description = job.description,
    requirements = job.requirements.split('\n').filter { it.isNotBlank() },
    deadline = LocalDate.ofEpochDay(job.deadlineEpochDay),
    applyUrl = job.applyUrl,
    isBookmarked = isBookmarked,
)
