package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass Palette - Dark Mode
val DarkBackground = Color(0xFF0A0F1D)
val DarkSurfaceGlass = Color(0x1AFFFFFF) // ~10% white
val DarkGlassBorderLight = Color(0x38FFFFFF)
val DarkGlassBorderDark = Color(0x0DFFFFFF)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Liquid Glass Palette - Light Mode
val LightBackground = Color(0xFFF1F5F9)
val LightSurfaceGlass = Color(0xB3FFFFFF) // ~70% white
val LightGlassBorderLight = Color(0xFFFFFFFF)
val LightGlassBorderDark = Color(0x26000000)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Accent Orange (Glowing Equals Button & Highlights)
val GlassOrangeTop = Color(0xFFFFB04A)
val GlassOrangeBottom = Color(0xFFF07F1E)
val GlassOrangeGlow = Color(0x80FF8C1A)

val OrangeEqualsBrush = Brush.verticalGradient(
    colors = listOf(GlassOrangeTop, GlassOrangeBottom)
)

// Sun & Moon Colors
val SunGlow = Color(0xFFFFC107)
val SunCenter = Color(0xFFFFE082)
val MoonCrater = Color(0xFFD1D5DB)
val MoonSphere = Color(0xFFE2E8F0)
val PillDarkBg = Color(0xFF0F172A)
val PillLightBg = Color(0xFFBAE6FD)

// Operator Key Glass Tint
val DarkOperatorGlass = Color(0x2EFFFFFF)
val LightOperatorGlass = Color(0xD9FFFFFF)

// Function Key Glass Tint (AC, %, Del)
val DarkFunctionGlass = Color(0x20FFFFFF)
val LightFunctionGlass = Color(0xC0FFFFFF)

// Ambient Moving Blob Colors
val BlobViolet = Color(0xFF7C3AED)
val BlobIndigo = Color(0xFF4F46E5)
val BlobCyan = Color(0xFF06B6D4)
val BlobAmber = Color(0xFFF59E0B)
val BlobPink = Color(0xFFEC4899)
val BlobBlue = Color(0xFF3B82F6)
