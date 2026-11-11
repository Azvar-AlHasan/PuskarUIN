package com.azvar.puskar.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azvar.puskar.domain.model.JobFilter
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.ui.components.JobCard

@Composable
fun JobsRoute(
    onJobClick: (String) -> Unit,
    viewModel: JobsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    JobsScreen(
        uiState = uiState,
        onJobClick = onJobClick,
        onQueryChange = viewModel::onQueryChange,
        onTypeSelected = viewModel::onTypeSelected,
        onBookmarkedOnlyToggle = viewModel::onBookmarkedOnlyToggle,
        onBookmarkClick = viewModel::onBookmarkClick,
        onRefresh = viewModel::refresh,
        onErrorShown = viewModel::onErrorShown,
    )
}

/** Stateless so it can be previewed and UI-tested without Hilt. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    uiState: JobsUiState,
    onJobClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onTypeSelected: (JobType?) -> Unit,
    onBookmarkedOnlyToggle: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("PuskarUIN") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(query = uiState.filter.query, onQueryChange = onQueryChange)
            FilterChips(
                filter = uiState.filter,
                onTypeSelected = onTypeSelected,
                onBookmarkedOnlyToggle = onBookmarkedOnlyToggle,
            )
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    uiState.jobs.isEmpty() -> EmptyState(uiState.filter)
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(uiState.jobs, key = { it.id }) { job ->
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
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari posisi, perusahaan, atau lokasi") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Hapus pencarian")
                }
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
}

@Composable
private fun FilterChips(
    filter: JobFilter,
    onTypeSelected: (JobType?) -> Unit,
    onBookmarkedOnlyToggle: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = filter.bookmarkedOnly,
                onClick = onBookmarkedOnlyToggle,
                label = { Text("Tersimpan") },
                leadingIcon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
            )
        }
        items(JobType.entries) { type ->
            FilterChip(
                selected = filter.type == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.label) },
            )
        }
    }
}

@Composable
private fun EmptyState(filter: JobFilter) {
    // LazyColumn so pull-to-refresh still works on an empty list.
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(32.dp)) {
        item {
            Text(
                text = when {
                    filter.bookmarkedOnly -> "Belum ada lowongan yang kamu simpan."
                    filter.query.isNotBlank() || filter.type != null -> "Tidak ada lowongan yang cocok dengan filter."
                    else -> "Belum ada lowongan. Tarik ke bawah untuk memuat ulang."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
