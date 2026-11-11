package com.azvar.puskar.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.ui.components.InfoRow
import com.azvar.puskar.ui.components.Tag
import com.azvar.puskar.ui.components.addToCalendar
import com.azvar.puskar.ui.components.formattedDate
import com.azvar.puskar.ui.components.formattedTimeRange
import com.azvar.puskar.ui.components.rememberNotificationPermissionGate
import com.azvar.puskar.ui.components.shareEvent

@Composable
fun EventDetailRoute(
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val withPermission = rememberNotificationPermissionGate(onDenied = viewModel::onNotificationPermissionDenied)
    EventDetailScreen(
        uiState = uiState,
        message = message,
        onBack = onBack,
        onReminderClick = { isSet ->
            if (isSet) viewModel.onReminderClick() else withPermission { viewModel.onReminderClick() }
        },
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    uiState: EventDetailUiState,
    message: String?,
    onBack: () -> Unit,
    onReminderClick: (isCurrentlySet: Boolean) -> Unit,
    onMessageShown: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState is EventDetailUiState.Success) {
                        IconButton(onClick = { context.shareEvent(uiState.event) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Bagikan event")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                EventDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                EventDetailUiState.NotFound -> Text(
                    "Event tidak ditemukan atau sudah dihapus.",
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                is EventDetailUiState.Success -> EventDetailContent(
                    event = uiState.event,
                    isPast = uiState.isPast,
                    onReminderClick = { onReminderClick(uiState.event.isReminderSet) },
                    onAddToCalendar = { context.addToCalendar(uiState.event) },
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: CareerEvent,
    isPast: Boolean,
    onReminderClick: () -> Unit,
    onAddToCalendar: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tag(event.category.label)
        Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        InfoRow(Icons.Outlined.Person, event.organizer)
        InfoRow(Icons.Outlined.CalendarMonth, event.formattedDate())
        InfoRow(Icons.Outlined.Schedule, event.formattedTimeRange())
        InfoRow(if (event.isOnline) Icons.Outlined.Videocam else Icons.Outlined.LocationOn, event.location)
        HorizontalDivider()
        Text("Tentang event", style = MaterialTheme.typography.titleMedium)
        Text(event.description, style = MaterialTheme.typography.bodyMedium)

        if (isPast) {
            Text(
                "Event ini sudah selesai.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Button(
                onClick = { uriHandler.openUri(event.registrationUrl) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Daftar Sekarang") }
            FilledTonalButton(onClick = onReminderClick, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    if (event.isReminderSet) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(if (event.isReminderSet) "Pengingat aktif (1 jam sebelum)" else "Ingatkan Saya")
            }
            OutlinedButton(onClick = onAddToCalendar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tambah ke Kalender")
            }
        }
    }
}
