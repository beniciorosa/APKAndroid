package com.empresa.dashboard.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.empresa.dashboard.R
import com.empresa.dashboard.data.models.SellerDto
import com.empresa.dashboard.ui.components.CustomDateRangePicker
import com.empresa.dashboard.ui.model.Period
import com.empresa.dashboard.ui.theme.AppTheme
import com.empresa.dashboard.ui.theme.ThemePalette
import com.empresa.dashboard.ui.theme.ThemePrefs
import com.empresa.dashboard.ui.theme.palette
import com.empresa.dashboard.ui.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentTheme by ThemePrefs.flow(ctx).collectAsState(initial = AppTheme.MONO)
    val colors = palette(currentTheme)
    var showDatePicker by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.escalada_wordmark),
                        contentDescription = "ESCALADA",
                        modifier = Modifier.height(28.dp),
                        colorFilter = ColorFilter.tint(colors.onBackground),
                    )
                },
                actions = {
                    IconButton(onClick = { showThemeSheet = true }) {
                        Icon(
                            Icons.Outlined.Palette,
                            contentDescription = "Tema",
                            tint = colors.onBackground,
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Atualizar",
                            tint = colors.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PeriodSelector(
                selected = state.period,
                colors = colors,
                onSelect = { period ->
                    if (period == Period.CUSTOM) showDatePicker = true
                    else viewModel.selectPeriod(period)
                },
            )

            TotalCard(
                total = state.total,
                dealCount = state.dealCount,
                periodLabel = state.periodLabel,
                updatedAt = state.updatedAt,
                isLoading = state.isLoading,
                colors = colors,
                theme = currentTheme,
            )

            state.error?.let { err -> ErrorCard(err, colors) }

            if (state.sellers.isNotEmpty()) {
                Text(
                    "Vendedores",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = colors.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                )
                SellerList(sellers = state.sellers, colors = colors)
            }
        }
    }

    if (showDatePicker) {
        CustomDateRangePicker(
            colors = colors,
            onDismiss = { showDatePicker = false },
            onConfirm = { from, to ->
                showDatePicker = false
                viewModel.selectPeriod(
                    Period.CUSTOM,
                    from.toString(),   // ISO yyyy-MM-dd
                    to.toString(),
                )
            },
        )
    }

    if (showThemeSheet) {
        ThemePickerSheet(
            currentTheme = currentTheme,
            colors = colors,
            onDismiss = { showThemeSheet = false },
            onPick = { theme ->
                scope.launch {
                    ThemePrefs.save(ctx, theme)
                    showThemeSheet = false
                }
            },
        )
    }
}

@Composable
private fun PeriodSelector(
    selected: Period,
    colors: ThemePalette,
    onSelect: (Period) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Period.values().forEach { period ->
            val isSel = selected == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSel) colors.onBackground else colors.surface)
                    .clickable(onClick = { onSelect(period) })
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    period.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSel) colors.background else colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun TotalCard(
    total: Double,
    dealCount: Int,
    periodLabel: String,
    updatedAt: String?,
    isLoading: Boolean,
    colors: ThemePalette,
    theme: AppTheme,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(colors.primaryGradientStart, colors.primaryGradientEnd))
            )
            .padding(28.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "FATURAMENTO",
                    color = (if (theme == AppTheme.MONO) Color.White else Color.White).copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    periodLabel.uppercase(),
                    color = (if (theme == AppTheme.MONO) Color.White else Color.White).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                )
            }
            Spacer(Modifier.height(12.dp))
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
            } else {
                Text(
                    CurrencyFormatter.format(total),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$dealCount negócios ganhos",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                )
                updatedAt?.let {
                    Text(
                        "  •  $it",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerList(sellers: List<SellerDto>, colors: ThemePalette) {
    val maxValue = sellers.maxOfOrNull { it.total } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sellers.forEachIndexed { idx, seller ->
            SellerRow(seller, maxValue, idx + 1, colors)
        }
    }
}

@Composable
private fun SellerRow(seller: SellerDto, maxValue: Double, rank: Int, colors: ThemePalette) {
    val progress = if (maxValue > 0) (seller.total / maxValue).toFloat() else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        "#$rank",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.muted,
                        modifier = Modifier.width(28.dp),
                    )
                    Text(
                        seller.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = colors.onSurface,
                    )
                }
                Text(
                    CurrencyFormatter.format(seller.total),
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                    fontSize = 15.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = colors.onBackground,
                trackColor = colors.surface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${seller.dealCount} ${if (seller.dealCount == 1) "deal" else "deals"}",
                fontSize = 11.sp,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, colors: ThemePalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEE2E2))
            .padding(16.dp),
    ) {
        Text("Erro: $message", color = Color(0xFF991B1B), fontSize = 13.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerSheet(
    currentTheme: AppTheme,
    colors: ThemePalette,
    onDismiss: () -> Unit,
    onPick: (AppTheme) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Escolha o tema",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = colors.onSurface,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            AppTheme.values().forEach { theme ->
                val isSel = theme == currentTheme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) colors.card else colors.surface)
                        .clickable { onPick(theme) }
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSel, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(theme.label, color = colors.onSurface, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

