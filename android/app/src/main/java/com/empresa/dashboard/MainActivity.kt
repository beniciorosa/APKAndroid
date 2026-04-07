package com.empresa.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.empresa.dashboard.ui.dashboard.DashboardScreen
import com.empresa.dashboard.ui.home.HomeScreen
import com.empresa.dashboard.ui.navigation.BottomNavBar
import com.empresa.dashboard.ui.navigation.NavRoute
import com.empresa.dashboard.ui.operational.OperationalScreen
import com.empresa.dashboard.ui.theme.AppTheme
import com.empresa.dashboard.ui.theme.ThemePrefs
import com.empresa.dashboard.ui.theme.palette
import androidx.glance.appwidget.updateAll
import com.empresa.dashboard.widget.RevenueWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // Atualizar widgets ao abrir o app
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try { RevenueWidget().updateAll(this@MainActivity) } catch (_: Exception) {}
        }

        setContent {
            val ctx = LocalContext.current
            val currentTheme by ThemePrefs.flow(ctx).collectAsState(initial = AppTheme.MONO)
            val colors = palette(currentTheme)
            val scope = rememberCoroutineScope()

            // Páginas: Operacional(0), Home(1), Comercial(2)
            val pages = NavRoute.values()
            val pagerState = rememberPagerState(initialPage = 1) { pages.size }

            val currentRoute by remember {
                derivedStateOf { pages[pagerState.currentPage] }
            }

            MaterialTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = colors.background,
                    bottomBar = {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            colors = colors,
                            onNavigate = { route ->
                                val idx = pages.indexOf(route)
                                scope.launch {
                                    // Scroll direto sem animação intermediária
                                    pagerState.scrollToPage(idx)
                                }
                            },
                        )
                    },
                ) { padding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(colors.background),
                        beyondViewportPageCount = 2,
                    ) { page ->
                        when (pages[page]) {
                            NavRoute.COMMERCIAL -> DashboardScreen()
                            NavRoute.HOME -> HomeScreen(
                                colors = colors,
                                onNavigateCommercial = {
                                    scope.launch { pagerState.scrollToPage(0) }
                                },
                            )
                            NavRoute.OPERATIONAL -> OperationalScreen(colors = colors)
                        }
                    }
                }
            }
        }
    }
}
