package com.azvar.puskar.domain

import com.azvar.puskar.domain.model.JobFilter
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.testing.testJob
import org.junit.Assert.assertEquals
import org.junit.Test

class JobFilterTest {

    private val jobs = listOf(
        testJob("1", title = "Android Developer Intern", type = JobType.INTERNSHIP),
        testJob("2", title = "Data Analyst", company = "Bank Sejahtera", type = JobType.FULL_TIME),
        testJob("3", title = "IT Support", location = "Depok", type = JobType.PART_TIME, isBookmarked = true),
    )

    @Test
    fun `empty filter returns everything`() {
        assertEquals(jobs, JobFilter().apply(jobs))
    }

    @Test
    fun `query matches title case-insensitively`() {
        assertEquals(listOf("1"), JobFilter(query = "android").apply(jobs).map { it.id })
    }

    @Test
    fun `query matches company and location`() {
        assertEquals(listOf("2"), JobFilter(query = "sejahtera").apply(jobs).map { it.id })
        assertEquals(listOf("3"), JobFilter(query = "depok").apply(jobs).map { it.id })
    }

    @Test
    fun `blank query is ignored`() {
        assertEquals(3, JobFilter(query = "   ").apply(jobs).size)
    }

    @Test
    fun `type filter keeps only that type`() {
        assertEquals(listOf("2"), JobFilter(type = JobType.FULL_TIME).apply(jobs).map { it.id })
    }

    @Test
    fun `bookmarked only keeps saved jobs`() {
        assertEquals(listOf("3"), JobFilter(bookmarkedOnly = true).apply(jobs).map { it.id })
    }

    @Test
    fun `filters combine with AND`() {
        assertEquals(
            emptyList<String>(),
            JobFilter(query = "android", type = JobType.FULL_TIME).apply(jobs).map { it.id },
        )
    }
}
