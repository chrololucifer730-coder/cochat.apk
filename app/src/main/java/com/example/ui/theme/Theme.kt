package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class ChatThemeCustomization(
    val selectedPalette: PaletteTheme = PaletteTheme.CYBER_CYAN,
    val isDarkMode: Boolean = true,
    val bubbleShape: BubbleShapeType = BubbleShapeType.ROUNDED_SLOPE,
    val wallpaperType: WallpaperType = WallpaperType.SUBTLE_GRID,
    val customWallpaperUri: String? = null,
    val wallpaperBrightness: Float = 0.85f,
    val wallpaperOpacity: Float = 0.9f,
    val wallpaperBlur: Float = 0f
)

enum class BubbleShapeType(val label: String) {
    ROUNDED_SLOPE("Modern WhatsApp"),
    SLEEK_GEOMETRIC("Tech Sharp"),
    PILL_OVAL("Soft Pill")
}

enum class WallpaperType(val label: String) {
    SUBTLE_GRID("Subtle Grid"),
    NEON_GEOMETRIC("Cyber Hex"),
    SOLID_DARK("Solid Dark"),
    MIDNIGHT_GRADIENT("Midnight Gradient"),
    WARM_DUSK("Warm Dusk"),
    CUSTOM_IMAGE("Custom Uploaded Image")
}

val LocalChatTheme = compositionLocalOf { ChatThemeCustomization() }

private fun getDarkScheme(palette: PaletteTheme) = darkColorScheme(
    primary = palette.primary,
    onPrimary = Color(0xFF0A0F1D),
    primaryContainer = palette.outgoingBubbleDark,
    onPrimaryContainer = Color.White,
    secondary = palette.secondary,
    onSecondary = Color(0xFF0A0F1D),
    tertiary = palette.accentColor,
    background = DarkNavyBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkNavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkNavyBorder
)

private fun getLightScheme(palette: PaletteTheme) = lightColorScheme(
    primary = palette.secondary,
    onPrimary = Color.White,
    primaryContainer = LightOutgoingBubble,
    onPrimaryContainer = palette.outgoingBubbleDark,
    secondary = palette.accentColor,
    onSecondary = Color.White,
    tertiary = palette.primary,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    customization: ChatThemeCustomization = ChatThemeCustomization(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = customization.isDarkMode
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkScheme(customization.selectedPalette)
        else -> getLightScheme(customization.selectedPalette)
    }

    CompositionLocalProvider(LocalChatTheme provides customization) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
