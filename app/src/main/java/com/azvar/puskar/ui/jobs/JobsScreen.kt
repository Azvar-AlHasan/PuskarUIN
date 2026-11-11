package com.azvar.puskar.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azvar.puskar.domain.model.JobFilter
import com.azvar.puskar.domain.model.JobSort
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.ui.components.EmptyState
import com.azvar.puskar.ui.components.JobCard
import com.azvar.puskar.ui.components.SkeletonList

@Composable
fun JobsRoute(
    onJobClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: JobsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    JobsScreen(
        uiState = uiState,
        onJobClick = onJobClick,
        onSettingsClick = onSettingsClick,
        onQueryChange = viewModel::onQueryChange,
        onTypeSelected = viewModel::onTypeSelected,
        onSortSelected = viewModel::onSortSelected,
        onClearFilters = viewModel::onClearFilters,
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
    onSettingsClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTypeSelected: (JobType?) -> Unit,
    onSortSelected: (JobSort) -> Unit,
    onClearFilters: () -> Unit,
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PuskarUIN", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Pusat Karier UIN Jakarta",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The outer scaffold (bottom navigation) already handles the bottom inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(query = uiState.filter.query, onQueryChange = onQueryChange)
            TypeChips(filter = uiState.filter, onTypeSelected = onTypeSelected)
            if (!uiState.isLoading) {
                ResultsHeader(
                    count = uiState.jobs.size,
                    sort = uiState.filter.sort,
                    onSortSelected = onSortSelected,
                )
            }
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.isLoading -> SkeletonList(Modifier.fillMaxSize())
                    uiState.jobs.isEmpty() -> LazyColumn(Modifier.fillMaxSize()) {
                        // Inside a LazyColumn so pull-to-refresh still works on an empty list.
                        item {
                            if (uiState.filter.isActive) {
                                EmptyState(
                                    icon = Icons.Outlined.SearchOff,
                                    title = "Tidak ada yang cocok",
                                    message = "Coba kata kunci lain atau hapus filter yang aktif.",
                                    actionLabel = "Hapus filter",
                                    onAction = onClearFilters,
                                )
                            } else {
                                EmptyState(
                                    icon = Icons.Outlined.WorkOutline,
                                    title = "Belum ada lowongan",
                                    message = "Lowongan baru dari Pusat Karier akan muncul di sini. Tarik ke bawah untuk memuat ulang.",
                                )
                            }
                        }
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
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
    val focusManager = LocalFocusManager.current
    TextField(
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
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
}

@Composable
private fun TypeChips(filter: JobFilter, onTypeSelected: (JobType?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = filter.type == null,
                onClick = { onTypeSelected(filter.type) }, // re-selecting the active type clears it
                label = { Text("Semua") },
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
private fun ResultsHeader(count: Int, sort: JobSort, onSortSelected: (JobSort) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$count lowongan",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Box {
            TextButton(onClick = { menuOpen = true }) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                Text(" ${sort.label}")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                JobSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortSelected(option)
                            menuOpen = false
                        },
                        trailingIcon = {
                            if (option == sort) Icon(Icons.Filled.Check, contentDescription = "Dipilih")
                        },
                    )
                }
            }
        }
    }
}
