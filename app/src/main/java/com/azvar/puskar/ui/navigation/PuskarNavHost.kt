package com.azvar.puskar.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.azvar.puskar.ui.events.EventDetailRoute
import com.azvar.puskar.ui.events.EventsRoute
import com.azvar.puskar.ui.jobdetail.JobDetailRoute
import com.azvar.puskar.ui.jobs.JobsRoute
import com.azvar.puskar.ui.saved.SavedRoute
import com.azvar.puskar.ui.settings.SettingsRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/** Type-safe navigation destinations. */
@Serializable
data object JobsDestination

@Serializable
data object EventsDestination

@Serializable
data object SavedDestination

@Serializable
data object SettingsDestination

@Serializable
data class JobDetailDestination(val jobId: String)

@Serializable
data class EventDetailDestination(val eventId: String)

private enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    JOBS(JobsDestination, JobsDestination::class, "Lowongan", Icons.Filled.Work, Icons.Outlined.WorkOutline),
    EVENTS(EventsDestination, EventsDestination::class, "Event", Icons.Filled.Event, Icons.Outlined.Event),
    SAVED(SavedDestination, SavedDestination::class, "Tersimpan", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
}

@Composable
fun PuskarNavHost(
    pendingEventId: String? = null,
    onPendingEventHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    // Deep link from a reminder notification straight to the event.
    LaunchedEffect(pendingEventId) {
        pendingEventId?.let {
            navController.navigate(EventDetailDestination(it))
            onPendingEventHandled()
        }
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isTopLevel = TopLevelDestination.entries.any { currentDestination?.hasRoute(it.routeClass) == true }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(dest.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    // Standard bottom-nav behaviour: keep one copy of each tab and restore its state.
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(if (selected) dest.selectedIcon else dest.unselectedIcon, contentDescription = null)
                            },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = JobsDestination,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable<JobsDestination> {
                JobsRoute(
                    onJobClick = { id -> navController.navigate(JobDetailDestination(id)) },
                    onSettingsClick = { navController.navigate(SettingsDestination) },
                )
            }
            composable<EventsDestination> {
                EventsRoute(onEventClick = { id -> navController.navigate(EventDetailDestination(id)) })
            }
            composable<SavedDestination> {
                SavedRoute(
                    onJobClick = { id -> navController.navigate(JobDetailDestination(id)) },
                    onEventClick = { id -> navController.navigate(EventDetailDestination(id)) },
                )
            }
            composable<JobDetailDestination> {
                JobDetailRoute(onBack = { navController.popBackStack() })
            }
            composable<EventDetailDestination> {
                EventDetailRoute(onBack = { navController.popBackStack() })
            }
            composable<SettingsDestination> {
                SettingsRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}
