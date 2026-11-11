package com.azvar.puskar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.azvar.puskar.core.designsystem.PuskarTheme
import com.azvar.puskar.ui.navigation.PuskarNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PuskarTheme {
                PuskarNavHost()
            }
        }
    }
}
