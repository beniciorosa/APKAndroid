package com.empresa.dashboard.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.empresa.dashboard.data.models.SellerDto
import com.empresa.dashboard.ui.model.Period
import com.empresa.dashboard.ui.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val BrandPrimary = Color(0xFF1E3A8A)
private val BrandSecondary = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faturamento", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PeriodSelector(
                selected = state.period,
                onSelect = { period ->
                    if (period == Period.CUSTOM) {
                        showDatePicker = true
                    } else {
                        viewModel.selectPeriod(period)
                    }
                },
            )

            TotalCard(
                total = state.total,
                dealCount = state.dealCount,
                periodLabel = state.periodLabel,
                updatedAt = state.updatedAt,
                isLoading = state.isLoading,
            )

            state.error?.let { err ->
                ErrorCard(err)
            }

            if (state.sellers.isNotEmpty()) {
                Text(
                    "Por vendedor",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
                SellerList(
                    sellers = state.sellers,
                    total = state.total,
                )
            }
        }
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { from, to ->
                showDatePicker = false
                viewModel.selectPeriod(Period.CUSTOM, from, to)
            },
        )
    }
}

@Composable
private fun PeriodSelector(selected: Period, onSelect: (Period) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Period.values().forEach { period ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelect(period) },
                label = { Text(period.label, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
            )
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
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))
            )
            .padding(24.dp),
    ) {
        Column {
            Text(
                periodLabel,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(8.dp))
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            } else {
                Text(
                    CurrencyFormatter.format(total),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$dealCount deals",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                )
                updatedAt?.let {
                    Text(
                        "  •  Atualizado $it",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerList(sellers: List<SellerDto>, total: Double) {
    val maxValue = sellers.maxOfOrNull { it.total } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sellers.forEach { seller ->
            SellerRow(seller, maxValue)
        }
    }
}

@Composable
private fun SellerRow(seller: SellerDto, maxValue: Double) {
    val progress = if (maxValue > 0) (seller.total / maxValue).toFloat() else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    seller.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    CurrencyFormatter.format(seller.total),
                    fontWeight = FontWeight.SemiBold,
                    color = BrandPrimary,
                    fontSize = 15.sp,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = BrandSecondary,
                trackColor = Color(0xFFE2E8F0),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${seller.dealCount} deals",
                fontSize = 11.sp,
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
    ) {
        Text(
            "Erro: $message",
            modifier = Modifier.padding(16.dp),
            color = Color(0xFF991B1B),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (from: String, to: String) -> Unit,
) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val from = state.selectedStartDateMillis
                    val to = state.selectedEndDateMillis
                    if (from != null && to != null) {
                        onConfirm(toIsoDate(from), toIsoDate(to))
                    }
                },
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
            ) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) {
        DateRangePicker(state = state, modifier = Modifier.height(500.dp))
    }
}

private fun toIsoDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}
