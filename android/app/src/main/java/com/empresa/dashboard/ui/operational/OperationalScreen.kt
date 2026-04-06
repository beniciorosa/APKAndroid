package com.empresa.dashboard.ui.operational

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empresa.dashboard.R
import com.empresa.dashboard.ui.theme.ThemePalette

@Composable
fun OperationalScreen(colors: ThemePalette) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.escalada_wordmark),
            contentDescription = "ESCALADA",
            modifier = Modifier.height(24.dp),
            colorFilter = ColorFilter.tint(colors.muted),
        )
        Spacer(Modifier.height(32.dp))
        Icon(
            Icons.Outlined.Construction,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Operacional",
            color = colors.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Em breve: tickets, clientes em atendimento, pipeline de entregas e mais.",
            color = colors.muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
    }
}
