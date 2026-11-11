package com.azvar.puskar.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.azvar.puskar.data.repository.EventRepository
import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.nowInEventZone
import com.azvar.puskar.ui.components.EmptyState
import com.azvar.puskar.ui.components.EventCard
import com.azvar.puskar.ui.components.JobCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class SavedUiState(
    val jobs: List<Job> = emptyList(),
    val events: List<CareerEvent> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isEmpty get() = jobs.isEmpty() && events.isEmpty()
}

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val eventRepository: EventRepository,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<SavedUiState> = combine(
        jobRepository.observeJobs(),
        eventRepository.observeEvents(),
    ) { jobs, events ->
        val now = clock.nowInEventZone()
        SavedUiState(
            jobs = jobs.filter { it.isBookmarked },
            // Only reminders that can still fire are worth showing here.
            events = events.filter { it.isReminderSet && !it.isPast(now) },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    fun onBookmarkClick(jobId: String) {
        viewModelScope.launch { jobRepository.toggleBookmark(jobId) }
    }

    fun onReminderClick(eventId: String) {
        viewModelScope.launch { eventRepository.toggleReminder(eventId) }
    }
}

@Composable
fun SavedRoute(
    onJobClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SavedScreen(
        uiState = uiState,
        onJobClick = onJobClick,
        onEventClick = onEventClick,
        onBookmarkClick = viewModel::onBookmarkClick,
        onReminderClick = viewModel::onReminderClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    uiState: SavedUiState,
    onJobClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onReminderClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Tersimpan") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.isEmpty -> EmptyState(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "Belum ada yang tersimpan",
                    message = "Tekan ikon bookmark pada lowongan atau ikon lonceng pada event untuk menyimpannya di sini.",
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (uiState.events.isNotEmpty()) {
                        item(key = "h-events") { SectionHeader("Pengingat event (${uiState.events.size})") }
                        items(uiState.events, key = { "e-${it.id}" }) { event ->
                            EventCard(
                                event = event,
                                isPast = false,
                                onClick = { onEventClick(event.id) },
                                onReminderClick = { onReminderClick(event.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                    if (uiState.jobs.isNotEmpty()) {
                        item(key = "h-jobs") { SectionHeader("Lowongan tersimpan (${uiState.jobs.size})") }
                        items(uiState.jobs, key = { "j-${it.id}" }) { job ->
                            JobCard(
                                job = job,
                                onClick = { onJobClick(job.id) },
                                onBookmarkClick = { onBookmarkClick(job.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().semantics { heading() },
    )
}
