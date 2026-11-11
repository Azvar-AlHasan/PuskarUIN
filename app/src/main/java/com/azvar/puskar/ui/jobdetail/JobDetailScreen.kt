package com.azvar.puskar.ui.jobdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.isClosed
import com.azvar.puskar.ui.components.CompanyLogo
import com.azvar.puskar.ui.components.Tag
import com.azvar.puskar.ui.components.deadlineLabel
import com.azvar.puskar.ui.components.formattedLong
import com.azvar.puskar.ui.components.relativePostedLabel
import com.azvar.puskar.ui.components.shareJob
import java.time.LocalDate

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
    today: LocalDate = LocalDate.now(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
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
        bottomBar = {
            if (uiState is JobDetailUiState.Success) {
                val closed = uiState.job.isClosed(today)
                Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Button(
                        onClick = { uriHandler.openUri(uiState.job.applyUrl) },
                        enabled = !closed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(48.dp),
                    ) {
                        Text(if (closed) "Pendaftaran sudah ditutup" else "Lamar Sekarang")
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                JobDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                JobDetailUiState.NotFound -> Text(
                    "Lowongan tidak ditemukan atau sudah dihapus.",
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                is JobDetailUiState.Success -> JobDetailContent(uiState.job, today)
            }
        }
    }
}

@Composable
private fun JobDetailContent(job: Job, today: LocalDate) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompanyLogo(job.company, size = 56.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(job.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(job.company, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag(job.type.label)
            if (job.industry.isNotBlank()) Tag(job.industry)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KeyFact(Icons.Outlined.LocationOn, "Lokasi", job.location)
                KeyFact(Icons.Outlined.Laptop, "Sistem kerja", job.workMode.label)
                KeyFact(Icons.Outlined.Payments, "Kompensasi", job.salary ?: "Tidak dicantumkan")
                KeyFact(Icons.Outlined.Event, "Batas pendaftaran", "${job.deadline.formattedLong()} (${deadlineLabel(job.deadline, today)})")
                KeyFact(Icons.Outlined.Schedule, "Diposting", relativePostedLabel(job.postedAt, today))
            }
        }

        Section("Deskripsi pekerjaan") {
            Text(job.description, style = MaterialTheme.typography.bodyMedium)
        }
        if (job.requirements.isNotEmpty()) {
            Section("Kualifikasi") { BulletList(job.requirements) }
        }
        if (job.benefits.isNotEmpty()) {
            Section("Benefit") { BulletList(job.benefits, checkmarks = true) }
        }
        if (job.companyAbout.isNotBlank()) {
            HorizontalDivider()
            Section("Tentang ${job.company}") {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(job.companyAbout, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun KeyFact(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}

@Composable
private fun BulletList(items: List<String>, checkmarks: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                if (checkmarks) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    )
                } else {
                    Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(10.dp))
                Text(item, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
