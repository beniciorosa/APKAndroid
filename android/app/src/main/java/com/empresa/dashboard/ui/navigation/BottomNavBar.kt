package com.empresa.dashboard.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.empresa.dashboard.R
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
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(colors.surface),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Operacional
            NavIcon(
                route = NavRoute.OPERATIONAL,
                isSelected = currentRoute == NavRoute.OPERATIONAL,
                colors = colors,
                onClick = { onNavigate(NavRoute.OPERATIONAL) },
            )

            // Home — logo Escalada com círculo degradê
            HomeIcon(
                isSelected = currentRoute == NavRoute.HOME,
                colors = colors,
                onClick = { onNavigate(NavRoute.HOME) },
            )

            // Comercial
            NavIcon(
                route = NavRoute.COMMERCIAL,
                isSelected = currentRoute == NavRoute.COMMERCIAL,
                colors = colors,
                onClick = { onNavigate(NavRoute.COMMERCIAL) },
            )
        }
    }
}

@Composable
private fun HomeIcon(
    isSelected: Boolean,
    colors: ThemePalette,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(colors.accentGradientStart, colors.accentGradientEnd)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.escalada_mark),
            contentDescription = "Home",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(
                if (isSelected) colors.background else colors.background.copy(alpha = 0.5f)
            ),
        )
    }
}

@Composable
private fun NavIcon(
    route: NavRoute,
    isSelected: Boolean,
    colors: ThemePalette,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) colors.card else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            route.icon,
            contentDescription = route.label,
            tint = if (isSelected) colors.onBackground else colors.muted,
            modifier = Modifier.size(24.dp),
        )
    }
}
