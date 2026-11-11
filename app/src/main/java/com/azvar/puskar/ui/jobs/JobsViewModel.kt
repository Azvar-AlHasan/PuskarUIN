package com.azvar.puskar.ui.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.JobFilter
import com.azvar.puskar.domain.model.JobSort
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.domain.model.nowInEventZone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class JobsUiState(
    val jobs: List<Job> = emptyList(),
    /** Number of jobs before filtering, for the "N lowongan" summary. */
    val totalCount: Int = 0,
    val filter: JobFilter = JobFilter(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** True until the first emission from the local cache arrives. */
    val isLoading: Boolean = true,
)

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repository: JobRepository,
    private val clock: Clock,
) : ViewModel() {

    private val filter = MutableStateFlow(JobFilter())
    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<JobsUiState> = combine(
        repository.observeJobs(),
        filter,
        refreshState,
    ) { jobs, currentFilter, refresh ->
        JobsUiState(
            jobs = currentFilter.apply(jobs, clock.nowInEventZone().toLocalDate()),
            totalCount = jobs.size,
            filter = currentFilter,
            isRefreshing = refresh.isRefreshing,
            errorMessage = refresh.errorMessage,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JobsUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshState.value = RefreshState(isRefreshing = true)
            val result = repository.refresh()
            refreshState.value = RefreshState(
                isRefreshing = false,
                errorMessage = result.exceptionOrNull()?.let { "Gagal memuat data terbaru" },
            )
        }
    }

    fun onQueryChange(query: String) = filter.update { it.copy(query = query) }

    /** Selecting the active type again clears it, like a toggle chip. */
    fun onTypeSelected(type: JobType?) =
        filter.update { it.copy(type = if (it.type == type) null else type) }

    fun onSortSelected(sort: JobSort) = filter.update { it.copy(sort = sort) }

    fun onClearFilters() = filter.update { JobFilter(sort = it.sort) }

    fun onBookmarkClick(jobId: String) {
        viewModelScope.launch { repository.toggleBookmark(jobId) }
    }

    fun onErrorShown() = refreshState.update { it.copy(errorMessage = null) }

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
    )
}
