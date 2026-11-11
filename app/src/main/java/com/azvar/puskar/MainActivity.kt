package com.azvar.puskar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.azvar.puskar.core.designsystem.PuskarTheme
import com.azvar.puskar.ui.navigation.PuskarNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Set when the app is opened from a reminder notification. */
    private var pendingEventId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) pendingEventId = intent.getStringExtra(EXTRA_EVENT_ID)
        setContent {
            PuskarTheme {
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
