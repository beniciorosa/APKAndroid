package com.empresa.dashboard.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empresa.dashboard.ui.theme.ThemePalette

@Composable
fun BottomNavBar(
    currentRoute: NavRoute,
    colors: ThemePalette,
    onNavigate: (NavRoute) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavRoute.values().forEach { route ->
                val isSelected = route == currentRoute
                val isHome = route == NavRoute.HOME

                if (isHome) {
                    // Home button — centralizado, maior, com fundo circular
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colors.onBackground
                                else colors.card
                            )
                            .clickable { onNavigate(route) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            route.icon,
                            contentDescription = route.label,
                            tint = if (isSelected) colors.background else colors.muted,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                } else {
                    // Abas laterais — menores que Home
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigate(route) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            route.icon,
                            contentDescription = route.label,
                            tint = if (isSelected) colors.onBackground else colors.muted,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            route.label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.onBackground else colors.muted,
                        )
                    }
                }
            }
        }
    }
}
