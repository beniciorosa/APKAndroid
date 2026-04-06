package com.empresa.dashboard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavRoute(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    COMMERCIAL("commercial", "Comercial", Icons.Filled.TrendingUp),
    HOME("home", "Home", Icons.Filled.Home),
    OPERATIONAL("operational", "Operacional", Icons.Outlined.Assignment),
}
