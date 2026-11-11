package com.azvar.puskar.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.ui.components.EmptyState
import com.azvar.puskar.ui.components.EventCard
import com.azvar.puskar.ui.components.SkeletonList
import com.azvar.puskar.ui.components.rememberNotificationPermissionGate

@Composable
fun EventsRoute(
    onEventClick: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val withPermission = rememberNotificationPermissionGate(onDenied = viewModel::onNotificationPermissionDenied)
    EventsScreen(
        uiState = uiState,
        onEventClick = onEventClick,
        onReminderClick = { event ->
            // Only turning a reminder ON needs the permission.
            if (event.isReminderSet) {
                viewModel.onReminderClick(event.id)
            } else {
                withPermission { viewModel.onReminderClick(event.id) }
            }
        },
        onModeSelected = viewModel::onModeSelected,
        onRefresh = viewModel::refresh,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    uiState: EventsUiState,
    onEventClick: (String) -> Unit,
    onReminderClick: (CareerEvent) -> Unit,
    onModeSelected: (EventModeFilter) -> Unit,
    onRefresh: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Event Karier") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EventModeFilter.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.modeFilter == mode,
                        onClick = { onModeSelected(mode) },
                        label = { Text(mode.label) },
                    )
                }
            }
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (uiState.isLoading) {
                    SkeletonList(Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (uiState.upcoming.isEmpty() && uiState.past.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Outlined.EventBusy,
                                    title = "Belum ada event",
                                    message = "Event karier dari Pusat Karier akan muncul di sini. Tarik ke bawah untuk memuat ulang.",
                                )
                            }
                        }
                        eventSection("Akan datang", uiState.upcoming, isPast = false, onEventClick, onReminderClick)
                        eventSection("Sudah lewat", uiState.past, isPast = true, onEventClick, onReminderClick)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.eventSection(
    title: String,
    events: List<CareerEvent>,
    isPast: Boolean,
    onEventClick: (String) -> Unit,
    onReminderClick: (CareerEvent) -> Unit,
) {
    if (events.isEmpty()) return
    item(key = "header-$title") {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
    }
    items(events, key = { it.id }) { event ->
        EventCard(
            event = event,
            isPast = isPast,
            onClick = { onEventClick(event.id) },
            onReminderClick = { onReminderClick(event) },
            modifier = Modifier.animateItem(),
        )
    }
}
