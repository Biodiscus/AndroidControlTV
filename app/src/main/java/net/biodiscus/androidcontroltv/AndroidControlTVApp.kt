package net.biodiscus.androidcontroltv

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import net.biodiscus.androidcontroltv.feature.home.HomeDestination
import net.biodiscus.androidcontroltv.feature.home.homeScreen

@Composable
internal fun AndroidControlTVApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeDestination) {
        homeScreen()
    }
}
