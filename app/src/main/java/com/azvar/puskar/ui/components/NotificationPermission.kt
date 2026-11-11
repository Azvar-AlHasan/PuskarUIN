package com.azvar.puskar.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Returns a function to call before turning a reminder ON.
 *
 * On Android 13+ it asks for the notification permission first. A reminder that can never
 * notify is useless, so the action only runs once permission is granted; otherwise [onDenied]
 * lets the screen explain what happened.
 */
@Composable
fun rememberNotificationPermissionGate(onDenied: () -> Unit): (proceed: () -> Unit) -> Unit {
    val context = LocalContext.current
    // Survives recomposition while the system dialog is open.
    val pending = remember { PendingAction() }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pending.action?.invoke() else onDenied()
        pending.action = null
    }
    return remember(launcher) {
        { proceed ->
            val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                pending.action = proceed
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                proceed()
            }
        }
    }
}

private class PendingAction {
    var action: (() -> Unit)? = null
}
