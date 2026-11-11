package com.azvar.puskar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azvar.puskar.ui.jobdetail.JobDetailRoute
import com.azvar.puskar.ui.jobs.JobsRoute
import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. */
@Serializable
data object JobsDestination

@Serializable
data class JobDetailDestination(val jobId: String)

@Composable
fun PuskarNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = JobsDestination) {
        composable<JobsDestination> {
            JobsRoute(onJobClick = { id -> navController.navigate(JobDetailDestination(id)) })
        }
        composable<JobDetailDestination> {
            JobDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}
