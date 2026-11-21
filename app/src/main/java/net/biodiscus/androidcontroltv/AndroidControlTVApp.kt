package net.biodiscus.androidcontroltv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import net.biodiscus.androidcontroltv.feature.debug.DebugDestination
import net.biodiscus.androidcontroltv.feature.debug.debugScreen
import net.biodiscus.androidcontroltv.feature.overview.OverviewDestination
import net.biodiscus.androidcontroltv.feature.overview.overviewScreen
import net.biodiscus.androidcontroltv.feature.remote.RemoteDestination
import net.biodiscus.androidcontroltv.feature.remote.remoteScreen

@Composable
internal fun AndroidControlTVApp(viewModel: AndroidControlTVAppViewModel = hiltViewModel()) {
    val startScreen by viewModel.startScreen.collectAsStateWithLifecycle()

    when (startScreen) {
        StartScreen.Loading -> Unit
        StartScreen.Overview -> AppNavigation(startDestination = OverviewDestination)
        StartScreen.Remote -> AppNavigation(startDestination = RemoteDestination)
    }
}

@Composable
private fun AppNavigation(startDestination: Any, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = startDestination) {
        overviewScreen(onConnected = { navController.navigateAsOnlyScreen(RemoteDestination) })
        remoteScreen(
            onBackClick = { navController.navigateAsOnlyScreen(OverviewDestination) },
            onDebugClick = { navController.navigate(DebugDestination) },
        )
        debugScreen(onBackClick = { navController.popBackStack() })
    }
}

private fun NavController.navigateAsOnlyScreen(destination: Any) {
    navigate(destination) {
        popUpTo(graph.id) { inclusive = true }
    }
}
