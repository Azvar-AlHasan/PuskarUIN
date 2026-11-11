package com.azvar.puskar.data.remote

import com.azvar.puskar.data.local.JobEntity
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Source of truth for job openings outside the device.
 *
 * The app ships with [com.azvar.puskar.data.remote.demo.DemoJobDataSource]. A backend
 * implementation (e.g. Firestore or a REST API) only needs to implement this interface and
 * replace one Hilt binding; repositories and UI stay unchanged.
 */
interface JobRemoteDataSource {
    suspend fun fetchJobs(): List<JobDto>
}

@Serializable
data class JobDto(
    val id: String,
    val title: String,
    val company: String,
    val industry: String = "",
    val companyAbout: String = "",
    val location: String,
    val type: String,
    val workMode: String,
    val description: String,
    val requirements: List<String>,
    val benefits: List<String> = emptyList(),
    val salary: String? = null,
    /** ISO-8601 date the opening was published. */
    val postedAt: String,
    /** ISO-8601 date, e.g. 2026-12-15 */
    val deadline: String,
    val applyUrl: String,
)

fun JobDto.toEntity(): JobEntity = JobEntity(
    id = id,
    title = title,
    company = company,
    industry = industry,
    companyAbout = companyAbout,
    location = location,
    type = type,
    workMode = workMode,
    description = description,
    requirements = requirements.joinToString("\n"),
    benefits = benefits.joinToString("\n"),
    salary = salary,
    postedEpochDay = LocalDate.parse(postedAt).toEpochDay(),
    deadlineEpochDay = LocalDate.parse(deadline).toEpochDay(),
    applyUrl = applyUrl,
)
