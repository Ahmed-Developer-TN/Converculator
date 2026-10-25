package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class GlassColorTokens(
    val isDark: Boolean,
    val background: Color,
    val surfaceGlass: Color,
    val borderLight: Color,
    val borderDark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val operatorGlass: Color,
    val functionGlass: Color,
    val equalsBrush: Brush,
    val navGlassBg: Color,
    val activePillGlass: Color
)

val DarkGlassTokens = GlassColorTokens(
    isDark = true,
    background = DarkBackground,
    surfaceGlass = DarkSurfaceGlass,
    borderLight = DarkGlassBorderLight,
    borderDark = DarkGlassBorderDark,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    operatorGlass = DarkOperatorGlass,
    functionGlass = DarkFunctionGlass,
    equalsBrush = OrangeEqualsBrush,
    navGlassBg = Color(0x33000000),
    activePillGlass = Color(0x33FFFFFF)
)

val LightGlassTokens = GlassColorTokens(
    isDark = false,
    background = LightBackground,
    surfaceGlass = LightSurfaceGlass,
    borderLight = LightGlassBorderLight,
    borderDark = LightGlassBorderDark,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    operatorGlass = LightOperatorGlass,
    functionGlass = LightFunctionGlass,
    equalsBrush = OrangeEqualsBrush,
    navGlassBg = Color(0xCCFFFFFF),
    activePillGlass = Color(0xFFFFFFFF)
)

val LocalGlassColors = staticCompositionLocalOf { DarkGlassTokens }

private val DarkColorScheme = darkColorScheme(
    primary = GlassOrangeTop,
    secondary = BlobIndigo,
    tertiary = BlobCyan,
    background = DarkBackground,
    surface = DarkSurfaceGlass,
    onPrimary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = GlassOrangeBottom,
    secondary = BlobBlue,
    tertiary = BlobCyan,
    background = LightBackground,
    surface = LightSurfaceGlass,
    onPrimary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val glassTokens = if (darkTheme) DarkGlassTokens else LightGlassTokens
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalGlassColors provides glassTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
