package com.empresa.dashboard.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppTheme(val key: String, val label: String) {
    MONO("mono", "Preto & Branco"),
    BLUE("blue", "Azul");

    companion object {
        fun fromKey(k: String?): AppTheme = values().find { it.key == k } ?: MONO
    }
}

object ThemeColors {
    // Mono
    val MonoBackground = Color(0xFF0A0A0A)
    val MonoSurface = Color(0xFF1A1A1A)
    val MonoCard = Color(0xFF242424)
    val MonoOnBackground = Color(0xFFFFFFFF)
    val MonoOnSurface = Color(0xFFE5E5E5)
    val MonoMuted = Color(0xFF737373)
    val MonoAccent = Color(0xFFFFFFFF)

    // Blue (tema anterior)
    val BluePrimary = Color(0xFF1E3A8A)
    val BlueSecondary = Color(0xFF3B82F6)
    val BlueBackground = Color(0xFFFFFFFF)
    val BlueSurface = Color(0xFFF8FAFC)
    val BlueOnBackground = Color(0xFF0F172A)
    val BlueMuted = Color(0xFF64748B)
}

data class ThemePalette(
    val background: Color,
    val surface: Color,
    val card: Color,
    val onBackground: Color,
    val onSurface: Color,
    val muted: Color,
    val primary: Color,
    val primaryGradientStart: Color,
    val primaryGradientEnd: Color,
    val onPrimary: Color,
)

fun palette(theme: AppTheme): ThemePalette = when (theme) {
    AppTheme.MONO -> ThemePalette(
        background = ThemeColors.MonoBackground,
        surface = ThemeColors.MonoSurface,
        card = ThemeColors.MonoCard,
        onBackground = ThemeColors.MonoOnBackground,
        onSurface = ThemeColors.MonoOnSurface,
        muted = ThemeColors.MonoMuted,
        primary = ThemeColors.MonoAccent,
        primaryGradientStart = Color(0xFF000000),
        primaryGradientEnd = Color(0xFF2A2A2A),
        onPrimary = Color(0xFF000000),
    )
    AppTheme.BLUE -> ThemePalette(
        background = ThemeColors.BlueBackground,
        surface = ThemeColors.BlueSurface,
        card = Color(0xFFF1F5F9),
        onBackground = ThemeColors.BlueOnBackground,
        onSurface = ThemeColors.BlueOnBackground,
        muted = ThemeColors.BlueMuted,
        primary = ThemeColors.BluePrimary,
        primaryGradientStart = ThemeColors.BluePrimary,
        primaryGradientEnd = ThemeColors.BlueSecondary,
        onPrimary = Color.White,
    )
}

private val Context.themeDataStore by preferencesDataStore("theme_prefs")
private val THEME_KEY = stringPreferencesKey("app_theme")

object ThemePrefs {
    fun flow(ctx: Context): Flow<AppTheme> =
        ctx.themeDataStore.data.map { AppTheme.fromKey(it[THEME_KEY]) }

    suspend fun save(ctx: Context, theme: AppTheme) {
        ctx.themeDataStore.edit { it[THEME_KEY] = theme.key }
    }
}
