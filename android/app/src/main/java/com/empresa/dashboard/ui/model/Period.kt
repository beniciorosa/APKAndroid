package com.empresa.dashboard.ui.model

enum class Period(val apiKey: String, val label: String) {
    THIS_MONTH("this-month", "Este mês"),
    LAST_30_DAYS("last-30-days", "Últimos 30 dias"),
    CUSTOM("custom", "Personalizado"),
}
