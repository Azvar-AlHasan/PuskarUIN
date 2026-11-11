package com.azvar.puskar.domain

import com.azvar.puskar.domain.model.JobFilter
import com.azvar.puskar.domain.model.JobSort
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.testing.testJob
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class JobFilterTest {

    private val today = LocalDate.of(2026, 11, 15)

    private val jobs = listOf(
        testJob("1", title = "Android Developer Intern", type = JobType.INTERNSHIP),
        testJob("2", title = "Data Analyst", company = "Bank Sejahtera", type = JobType.FULL_TIME),
        testJob("3", title = "IT Support", location = "Depok", type = JobType.PART_TIME, isBookmarked = true),
    )

    @Test
    fun `empty filter returns everything`() {
        assertEquals(jobs.toSet(), JobFilter().apply(jobs, today).toSet())
    }

    @Test
    fun `query matches industry`() {
        val withIndustry = jobs + testJob("4", title = "Teller", industry = "Perbankan")
        assertEquals(listOf("4"), JobFilter(query = "perbankan").apply(withIndustry, today).map { it.id })
    }

    @Test
    fun `closed openings go to the bottom regardless of sort`() {
        val list = listOf(
            testJob("closed", deadline = today.minusDays(1), postedAt = today),
            testJob("later", deadline = today.plusDays(20), postedAt = today.minusDays(9)),
            testJob("soon", deadline = today.plusDays(2), postedAt = today.minusDays(5)),
        )
        assertEquals(listOf("soon", "later", "closed"), JobFilter(sort = JobSort.DEADLINE).apply(list, today).map { it.id })
        assertEquals(listOf("soon", "later", "closed"), JobFilter(sort = JobSort.NEWEST).apply(list, today).map { it.id })
    }

    @Test
    fun `query matches title case-insensitively`() {
        assertEquals(listOf("1"), JobFilter(query = "android").apply(jobs, today).map { it.id })
    }

    @Test
    fun `query matches company and location`() {
        assertEquals(listOf("2"), JobFilter(query = "sejahtera").apply(jobs, today).map { it.id })
        assertEquals(listOf("3"), JobFilter(query = "depok").apply(jobs, today).map { it.id })
    }

    @Test
    fun `blank query is ignored`() {
        assertEquals(3, JobFilter(query = "   ").apply(jobs, today).size)
    }

    @Test
    fun `type filter keeps only that type`() {
        assertEquals(listOf("2"), JobFilter(type = JobType.FULL_TIME).apply(jobs, today).map { it.id })
    }

    @Test
    fun `bookmarked only keeps saved jobs`() {
        assertEquals(listOf("3"), JobFilter(bookmarkedOnly = true).apply(jobs, today).map { it.id })
    }

    @Test
    fun `filters combine with AND`() {
        assertEquals(
            emptyList<String>(),
            JobFilter(query = "android", type = JobType.FULL_TIME).apply(jobs, today).map { it.id },
        )
    }
}
