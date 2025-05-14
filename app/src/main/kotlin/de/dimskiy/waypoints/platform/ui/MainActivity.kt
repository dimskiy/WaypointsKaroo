package de.dimskiy.waypoints.platform.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.dimskiy.waypoints.platform.ui.components.NavigationOverlay
import de.dimskiy.waypoints.platform.ui.screens.Screen
import de.dimskiy.waypoints.platform.ui.screens.waypointslist.WaypointsListScreen
import de.dimskiy.waypoints.platform.ui.theme.AppTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.WaypointsList.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable(Screen.WaypointsList.route) {
                        NavigationOverlay {
                            WaypointsListScreen()
                        }
                    }
                }
            }
        }
    }

    private fun setupSplashScreen() {
        val splash = installSplashScreen()
        var isKeepSplash = true

        splash.setKeepOnScreenCondition { isKeepSplash }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.readyState.collect { isKeepSplash = false }
            }
        }
    }
}
