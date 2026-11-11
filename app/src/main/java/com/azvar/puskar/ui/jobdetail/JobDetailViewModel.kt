package com.azvar.puskar.ui.jobdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.ui.navigation.JobDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JobDetailUiState {
    data object Loading : JobDetailUiState
    data object NotFound : JobDetailUiState
    data class Success(val job: Job) : JobDetailUiState
}

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JobRepository,
) : ViewModel() {

    private val jobId = savedStateHandle.toRoute<JobDetailDestination>().jobId

    val uiState: StateFlow<JobDetailUiState> = repository.observeJob(jobId)
        .map { job -> if (job == null) JobDetailUiState.NotFound else JobDetailUiState.Success(job) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobDetailUiState.Loading)

    fun onBookmarkClick() {
        viewModelScope.launch { repository.toggleBookmark(jobId) }
    }
}
