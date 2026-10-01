package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ChatThemeCustomization
import com.example.ui.theme.WallpaperType

@Composable
fun WallpaperContainer(
    theme: ChatThemeCustomization,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = theme.isDarkMode
    val baseBg = if (isDark) Color(0xFF0A0F1D) else Color(0xFFF1F5F9)

    Box(modifier = modifier.fillMaxSize().background(baseBg)) {
        when (theme.wallpaperType) {
            WallpaperType.SUBTLE_GRID -> {
                val lineColor = if (isDark) Color(0xFF1E2D4A).copy(alpha = 0.25f * theme.wallpaperOpacity)
                else Color(0xFFCBD5E1).copy(alpha = 0.4f * theme.wallpaperOpacity)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 36.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += step
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += step
                    }
                }
            }

            WallpaperType.NEON_GEOMETRIC -> {
                val accent = theme.selectedPalette.primary.copy(alpha = 0.12f * theme.wallpaperOpacity)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = 40.dp.toPx()
                    var x = 0f
                    while (x < size.width + r) {
                        var y = 0f
                        while (y < size.height + r) {
                            drawCircle(accent, radius = 2.dp.toPx(), center = Offset(x, y))
                            drawCircle(accent, radius = r * 0.4f, center = Offset(x, y), style = Stroke(width = 1f))
                            y += r * 1.5f
                        }
                        x += r * 1.5f
                    }
                }
            }

            WallpaperType.MIDNIGHT_GRADIENT -> {
                val gradBrush = Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0xFF0F172A), Color(0xFF061826), Color(0xFF0A0F1D))
                    else listOf(Color(0xFFE2E8F0), Color(0xFFE0F2FE), Color(0xFFF8FAFC))
                )
                Box(modifier = Modifier.fillMaxSize().background(gradBrush))
            }

            WallpaperType.WARM_DUSK -> {
                val warmBrush = Brush.linearGradient(
                    colors = if (isDark) listOf(Color(0xFF1A102F), Color(0xFF0B172E), Color(0xFF0A0F1D))
                    else listOf(Color(0xFFFFEDD5), Color(0xFFFEF3C7), Color(0xFFF8FAFC))
                )
                Box(modifier = Modifier.fillMaxSize().background(warmBrush))
            }

            WallpaperType.SOLID_DARK -> {
                // Flat clean background
            }

            WallpaperType.CUSTOM_IMAGE -> {
                // Custom JPG/JPEG preview layer with brightness and opacity overlay
                val customGrad = Brush.radialGradient(
                    colors = listOf(
                        theme.selectedPalette.primary.copy(alpha = 0.2f * theme.wallpaperOpacity),
                        Color(0xFF050B14)
                    )
                )
                Box(modifier = Modifier.fillMaxSize().background(customGrad))
            }
        }

        // Overlay brightness adjuster
        if (theme.wallpaperBrightness < 1.0f) {
            val dimAlpha = (1.0f - theme.wallpaperBrightness).coerceIn(0f, 0.8f)
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = dimAlpha)))
        }

        content()
    }
}
