package com.empresa.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.empresa.dashboard.ui.theme.ThemePalette
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val PT_BR = Locale("pt", "BR")

@Composable
fun CustomDateRangePicker(
    colors: ThemePalette,
    onDismiss: () -> Unit,
    onConfirm: (from: LocalDate, to: LocalDate) -> Unit,
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surface)
                .padding(20.dp),
        ) {
            Column {
                // Header
                Text(
                    "SELECIONE O PERÍODO",
                    color = colors.muted,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    buildHeadline(startDate, endDate),
                    color = colors.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(20.dp))

                // Month navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavButton(colors, Icons.Filled.ChevronLeft, "Anterior") {
                        currentMonth = currentMonth.minusMonths(1)
                    }
                    Text(
                        currentMonth.month.getDisplayName(TextStyle.FULL, PT_BR)
                            .replaceFirstChar { it.uppercase() } + " " + currentMonth.year,
                        color = colors.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    NavButton(colors, Icons.Filled.ChevronRight, "Próximo") {
                        currentMonth = currentMonth.plusMonths(1)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    val weekdays = listOf("D", "S", "T", "Q", "Q", "S", "S")
                    weekdays.forEach { wd ->
                        Box(
                            modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                wd,
                                color = colors.muted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Days grid
                CalendarGrid(
                    yearMonth = currentMonth,
                    startDate = startDate,
                    endDate = endDate,
                    colors = colors,
                    onDayClick = { date ->
                        when {
                            startDate == null -> startDate = date
                            endDate == null -> {
                                if (date.isBefore(startDate)) {
                                    endDate = startDate
                                    startDate = date
                                } else {
                                    endDate = date
                                }
                            }
                            else -> {
                                startDate = date
                                endDate = null
                            }
                        }
                    },
                )

                Spacer(Modifier.height(20.dp))

                // Quick shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Shortcut("Limpar", colors, Modifier.weight(1f)) {
                        startDate = null; endDate = null
                    }
                    Shortcut("Hoje", colors, Modifier.weight(1f)) {
                        val today = LocalDate.now()
                        startDate = today; endDate = today
                        currentMonth = YearMonth.from(today)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = colors.muted, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                    val enabled = startDate != null && endDate != null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (enabled) colors.onBackground else colors.card)
                            .clickable(enabled = enabled) {
                                onConfirm(startDate!!, endDate!!)
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(
                            "Aplicar",
                            color = if (enabled) colors.background else colors.muted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    colors: ThemePalette,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = colors.onBackground, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Shortcut(label: String, colors: ThemePalette, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = colors.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    startDate: LocalDate?,
    endDate: LocalDate?,
    colors: ThemePalette,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // getDayOfWeek: MONDAY=1..SUNDAY=7. Queremos começar no domingo (0..6)
    val firstDayOffset = firstDay.dayOfWeek.value % 7 // SUNDAY(7)%7=0, MONDAY(1)%7=1, etc

    val totalCells = firstDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOffset + 1
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNumber)
                            DayCell(
                                date = date,
                                startDate = startDate,
                                endDate = endDate,
                                colors = colors,
                                onClick = { onDayClick(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    startDate: LocalDate?,
    endDate: LocalDate?,
    colors: ThemePalette,
    onClick: () -> Unit,
) {
    val isStart = date == startDate
    val isEnd = date == endDate
    val isInRange = startDate != null && endDate != null &&
        date.isAfter(startDate) && date.isBefore(endDate)
    val isSelected = isStart || isEnd
    val isToday = date == LocalDate.now()

    val bg = when {
        isSelected -> colors.onBackground
        isInRange -> colors.card
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> colors.background
        isInRange -> colors.onBackground
        else -> colors.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

private fun buildHeadline(from: LocalDate?, to: LocalDate?): String {
    val fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM", PT_BR)
    return when {
        from == null -> "Selecione as datas"
        to == null -> fmt.format(from) + " → ..."
        else -> "${fmt.format(from)} → ${fmt.format(to)}"
    }
}
