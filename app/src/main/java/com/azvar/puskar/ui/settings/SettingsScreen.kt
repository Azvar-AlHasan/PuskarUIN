package com.azvar.puskar.ui.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.azvar.puskar.BuildConfig
import com.azvar.puskar.data.settings.AppSettings
import com.azvar.puskar.data.settings.SettingsRepository
import com.azvar.puskar.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

const val CAREER_CENTER_URL = "https://karir.uinjkt.ac.id"
const val SOURCE_CODE_URL = "https://github.com/Azvar-AlHasan/PuskarUIN"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun onDynamicColorChange(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }
}

@Composable
fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onBack = onBack,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onDynamicColorChange = viewModel::onDynamicColorChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            SectionTitle("Tampilan")
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    ListItem(
                        headlineContent = { Text(mode.label) },
                        leadingContent = { RadioButton(selected = settings.themeMode == mode, onClick = null) },
                        modifier = Modifier.selectable(
                            selected = settings.themeMode == mode,
                            role = Role.RadioButton,
                            onClick = { onThemeModeSelected(mode) },
                        ),
                    )
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ListItem(
                    headlineContent = { Text("Warna dinamis") },
                    supportingContent = { Text("Ikuti warna wallpaper (Material You)") },
                    leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                    trailingContent = { Switch(checked = settings.dynamicColor, onCheckedChange = null) },
                    modifier = Modifier.toggleable(
                        value = settings.dynamicColor,
                        role = Role.Switch,
                        onValueChange = onDynamicColorChange,
                    ),
                )
            }
            ListItem(
                headlineContent = { Text("Notifikasi") },
                supportingContent = { Text("Atur izin dan suara pengingat event") },
                leadingContent = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                modifier = Modifier.clickableRow {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                },
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionTitle("Tentang")
            ListItem(
                headlineContent = { Text("PuskarUIN") },
                supportingContent = {
                    Text(
                        "Info lowongan, magang, dan event karier dari Pusat Karier " +
                            "UIN Syarif Hidayatullah Jakarta dalam satu aplikasi.",
                    )
                },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
            )
            ListItem(
                headlineContent = { Text("Versi") },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                leadingContent = { Icon(Icons.Outlined.NewReleases, contentDescription = null) },
            )
            LinkItem("Situs Pusat Karier UIN Jakarta", "karir.uinjkt.ac.id", Icons.Outlined.Language) {
                uriHandler.openUri(CAREER_CENTER_URL)
            }
            LinkItem("Kode sumber", "GitHub · Lisensi MIT", Icons.Outlined.Code) {
                uriHandler.openUri(SOURCE_CODE_URL)
            }
            Text(
                "Data lowongan dan event pada versi ini merupakan data demonstrasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun LinkItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
        modifier = Modifier.clickableRow(onClick),
    )
}

private fun Modifier.clickableRow(onClick: () -> Unit) = clickable(role = Role.Button, onClick = onClick)
