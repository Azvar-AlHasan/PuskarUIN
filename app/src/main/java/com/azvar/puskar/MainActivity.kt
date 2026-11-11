package com.azvar.puskar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.azvar.puskar.core.designsystem.PuskarTheme
import com.azvar.puskar.data.settings.AppSettings
import com.azvar.puskar.data.settings.SettingsRepository
import com.azvar.puskar.data.settings.ThemeMode
import com.azvar.puskar.ui.navigation.PuskarNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    /** Set when the app is opened from a reminder notification. */
    private var pendingEventId by mutableStateOf<String?>(null)

    /** Null until settings are read, so the splash screen covers the brief load and the theme never flashes. */
    private var settings by mutableStateOf<AppSettings?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { settings == null }
        enableEdgeToEdge()
        if (savedInstanceState == null) pendingEventId = intent.getStringExtra(EXTRA_EVENT_ID)

        // Collected outside Compose: the splash screen holds the first frame, so a
        // collector that needs a frame to start would wait forever.
        lifecycleScope.launch {
            settingsRepository.settings.collect { settings = it }
        }

        setContent {
            val current = settings ?: return@setContent
            val dark = when (current.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            PuskarTheme(darkTheme = dark, dynamicColor = current.dynamicColor) {
                PuskarNavHost(
                    pendingEventId = pendingEventId,
                    onPendingEventHandled = { pendingEventId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_EVENT_ID)?.let { pendingEventId = it }
    }

    companion object {
        const val EXTRA_EVENT_ID = "com.azvar.puskar.EXTRA_EVENT_ID"
    }
}
