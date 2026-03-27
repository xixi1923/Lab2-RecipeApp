package com.recipeapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange400 = Color(0xFFFF7043)
val Orange600 = Color(0xFFE64A19)
val Amber100 = Color(0xFFFFF8E1)
val Amber200 = Color(0xFFFFECB3)
val Brown900  = Color(0xFF3E2723)
val Brown700  = Color(0xFF5D4037)
val Surface0  = Color(0xFFFFFBF8)
val Surface1  = Color(0xFFF5EDE6)

private val LightColorScheme = lightColorScheme(
    primary = Orange400,
    onPrimary = Color.White,
    primaryContainer = Amber200,
    onPrimaryContainer = Brown900,
    secondary = Brown700,
    onSecondary = Color.White,
    background = Surface0,
    surface = Surface0,
    surfaceVariant = Surface1,
    onBackground = Brown900,
    onSurface = Brown900,
    error = Color(0xFFB00020)
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    onPrimary = Brown900,
    primaryContainer = Orange600,
    onPrimaryContainer = Amber100,
    secondary = Amber200,
    onSecondary = Brown900,
    background = Color(0xFF1C1410),
    surface = Color(0xFF261E18),
    onBackground = Amber100,
    onSurface = Amber100
)

@Composable
fun RecipeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
