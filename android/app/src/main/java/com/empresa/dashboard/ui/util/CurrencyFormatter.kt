package com.empresa.dashboard.ui.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val brl: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun format(value: Double): String = brl.format(value)

    fun formatCompact(value: Double): String = when {
        value >= 1_000_000 -> "R$ %.1fM".format(value / 1_000_000).replace('.', ',')
        value >= 1_000 -> "R$ %.1fK".format(value / 1_000).replace('.', ',')
        else -> brl.format(value)
    }
}
