package com.empresa.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.empresa.dashboard.ui.dashboard.DashboardScreen
import com.empresa.dashboard.ui.home.HomeScreen
import com.empresa.dashboard.ui.navigation.BottomNavBar
import com.empresa.dashboard.ui.navigation.NavRoute
import com.empresa.dashboard.ui.operational.OperationalScreen
import com.empresa.dashboard.ui.theme.AppTheme
import com.empresa.dashboard.ui.theme.ThemePrefs
import com.empresa.dashboard.ui.theme.palette
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            val currentTheme by ThemePrefs.flow(ctx).collectAsState(initial = AppTheme.MONO)
            val colors = palette(currentTheme)
            val navController = rememberNavController()
            var currentRoute by remember { mutableStateOf(NavRoute.HOME) }

            MaterialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = colors.background,
                    bottomBar = {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            colors = colors,
                            onNavigate = { route ->
                                currentRoute = route
                                navController.navigate(route.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    },
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavRoute.HOME.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(colors.background),
                    ) {
                        composable(NavRoute.HOME.route) {
                            HomeScreen(
                                colors = colors,
                                onNavigateCommercial = {
                                    currentRoute = NavRoute.COMMERCIAL
                                    navController.navigate(NavRoute.COMMERCIAL.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                        composable(NavRoute.COMMERCIAL.route) {
                            DashboardScreen()
                        }
                        composable(NavRoute.OPERATIONAL.route) {
                            OperationalScreen(colors = colors)
                        }
                    }
                }
            }
        }
    }
}
