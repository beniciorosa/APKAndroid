package com.empresa.dashboard.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.empresa.dashboard.R
import com.empresa.dashboard.ui.theme.ThemePalette
import com.empresa.dashboard.ui.util.CurrencyFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    colors: ThemePalette,
    onNavigateCommercial: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header: logo + refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.escalada_wordmark),
                contentDescription = "ESCALADA",
                modifier = Modifier.height(24.dp),
                colorFilter = ColorFilter.tint(colors.onBackground),
            )
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, "Atualizar", tint = colors.onBackground)
            }
        }

        // Saudação
        Text(
            greeting(),
            color = colors.muted,
            fontSize = 14.sp,
        )

        // Card faturamento HOJE
        MetricCard(
            label = "FATURAMENTO HOJE",
            value = if (state.isLoading) "..." else CurrencyFormatter.format(state.todayTotal),
            subtitle = "${state.todayDeals} negócios ganhos",
            colors = colors,
            gradientStart = colors.primaryGradientStart,
            gradientEnd = colors.primaryGradientEnd,
            onClick = onNavigateCommercial,
        )

        // Card faturamento MÊS
        MetricCard(
            label = "FATURAMENTO DO MÊS",
            value = if (state.isLoading) "..." else CurrencyFormatter.format(state.monthTotal),
            subtitle = "${state.monthDeals} negócios ganhos",
            colors = colors,
            gradientStart = colors.card,
            gradientEnd = colors.card,
            textColor = colors.onBackground,
            labelColor = colors.muted,
            onClick = onNavigateCommercial,
        )

        // Atalhos rápidos
        Text(
            "ATALHOS",
            color = colors.muted,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShortcutCard(
                icon = Icons.Filled.TrendingUp,
                label = "Comercial",
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = onNavigateCommercial,
            )
            ShortcutCard(
                icon = Icons.Outlined.Assignment,
                label = "Operacional",
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { /* placeholder */ },
            )
        }

        // Erro
        state.error?.let { err ->
            Text("Erro: $err", color = Color(0xFFEF4444), fontSize = 12.sp)
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    subtitle: String,
    colors: ThemePalette,
    gradientStart: Color,
    gradientEnd: Color,
    textColor: Color = Color.White,
    labelColor: Color = Color.White.copy(alpha = 0.6f),
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
            .clickable(onClick = onClick)
            .padding(24.dp),
    ) {
        Column {
            Text(
                label,
                color = labelColor,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                value,
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                color = textColor.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ShortcutCard(
    icon: ImageVector,
    label: String,
    colors: ThemePalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = colors.onBackground, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
}
