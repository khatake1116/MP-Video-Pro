package com.mpvideopro.player.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpvideopro.player.ui.screens.FileBrowserScreen
import com.mpvideopro.player.ui.screens.PlayerScreen
import com.mpvideopro.player.ui.screens.RecentlyPlayedScreen
import com.mpvideopro.player.ui.screens.SettingsScreen

/**
 * Main navigation setup for MP Video Pro.
 * Defines all screens and navigation between them.
 */
@Composable
fun MPVideoProNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.FileBrowser.route,
        modifier = modifier
    ) {
        composable(Screen.FileBrowser.route) {
            FileBrowserScreen(
                onVideoClick = { videoFile ->
                    navController.navigate(Screen.Player.createRoute(videoFile.uri.toString()))
                },
                onNavigateToRecentlyPlayed = {
                    navController.navigate(Screen.RecentlyPlayed.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.RecentlyPlayed.route) {
            RecentlyPlayedScreen(
                onVideoClick = { videoFile ->
                    navController.navigate(Screen.Player.createRoute(videoFile.uri.toString()))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Player.route) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri")
            if (videoUri != null) {
                PlayerScreen(
                    videoUri = videoUri,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Sealed class defining all app screens
 */
sealed class Screen(val route: String) {
    object FileBrowser : Screen("file_browser")
    object RecentlyPlayed : Screen("recently_played")
    object Settings : Screen("settings")
    object Player : Screen("player") {
        const val VIDEO_URI_ARG = "videoUri"
        fun createRoute(videoUri: String) = "player?$VIDEO_URI_ARG=$videoUri"
        fun getRoute() = "player?$VIDEO_URI_ARG={$VIDEO_URI_ARG}"
    }
}
