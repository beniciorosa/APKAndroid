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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Barra pill flutuante
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(colors.surface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavRoute.values().forEach { route ->
                val isSelected = route == currentRoute
                val isHome = route == NavRoute.HOME

                if (isHome) {
                    HomeButton(
                        isSelected = isSelected,
                        colors = colors,
                        onClick = { onNavigate(route) },
                    )
                } else {
                    SideTab(
                        route = route,
                        isSelected = isSelected,
                        colors = colors,
                        onClick = { onNavigate(route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeButton(
    isSelected: Boolean,
    colors: ThemePalette,
    onClick: () -> Unit,
) {
    val gradient = if (isSelected) {
        Brush.linearGradient(listOf(colors.accentGradientStart, colors.accentGradientEnd))
    } else {
        Brush.linearGradient(listOf(colors.card, colors.card))
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(gradient)
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
            modifier = Modifier.size(26.dp),
            colorFilter = ColorFilter.tint(
                if (isSelected) colors.background else colors.muted
            ),
        )
    }
}

@Composable
private fun SideTab(
    route: NavRoute,
    isSelected: Boolean,
    colors: ThemePalette,
    onClick: () -> Unit,
) {
    val textGradient = if (isSelected) {
        Brush.linearGradient(listOf(colors.accentGradientStart, colors.accentGradientEnd))
    } else {
        Brush.linearGradient(listOf(colors.muted, colors.muted))
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            route.icon,
            contentDescription = route.label,
            tint = if (isSelected) colors.onBackground else colors.muted,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            route.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.onBackground else colors.muted,
        )
        // Indicador de seleção com degradê
        if (isSelected) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(textGradient),
            )
        }
    }
}
