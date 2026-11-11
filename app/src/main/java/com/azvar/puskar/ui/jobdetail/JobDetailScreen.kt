package com.azvar.puskar.ui.jobdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.ui.components.shareJob
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JobDetailRoute(
    onBack: () -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    JobDetailScreen(uiState = uiState, onBack = onBack, onBookmarkClick = viewModel::onBookmarkClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    uiState: JobDetailUiState,
    onBack: () -> Unit,
    onBookmarkClick: () -> Unit,
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Lowongan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (uiState is JobDetailUiState.Success) {
                        IconButton(onClick = { context.shareJob(uiState.job) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Bagikan lowongan")
                        }
                        IconButton(onClick = onBookmarkClick) {
                            Icon(
                                imageVector = if (uiState.job.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (uiState.job.isBookmarked) "Hapus dari simpanan" else "Simpan lowongan",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                JobDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                JobDetailUiState.NotFound -> Text(
                    "Lowongan tidak ditemukan atau sudah dihapus.",
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                is JobDetailUiState.Success -> JobDetailContent(uiState.job)
            }
        }
    }
}

private val deadlineFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID"))

@Composable
private fun JobDetailContent(job: Job) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(job.company, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(job.type.label) })
            AssistChip(onClick = {}, label = { Text(job.workMode.label) })
            AssistChip(onClick = {}, label = { Text(job.location) })
        }
        Text(
            "Batas pendaftaran: ${job.deadline.format(deadlineFormatter)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        HorizontalDivider()
        Text("Deskripsi", style = MaterialTheme.typography.titleMedium)
        Text(job.description, style = MaterialTheme.typography.bodyMedium)
        if (job.requirements.isNotEmpty()) {
            Text("Kualifikasi", style = MaterialTheme.typography.titleMedium)
            job.requirements.forEach { req ->
                Text("•  $req", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Button(
            onClick = { uriHandler.openUri(job.applyUrl) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("Lamar Sekarang")
        }
    }
}
