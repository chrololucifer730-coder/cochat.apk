package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Boyish Messenger Palette (Cyber Slate & Electric Cyan)
val DarkNavyBackground = Color(0xFF0A0F1D)
val DarkNavySurface = Color(0xFF131D31)
val DarkNavySurfaceVariant = Color(0xFF1E2D4A)
val DarkNavyCard = Color(0xFF18243C)
val DarkNavyBorder = Color(0xFF283A5E)

val ElectricCyanPrimary = Color(0xFF00E5FF)
val ElectricCyanSecondary = Color(0xFF38BDF8)
val ElectricCyanTertiary = Color(0xFF0284C7)

val DarkOutgoingBubble = Color(0xFF0369A1)
val DarkIncomingBubble = Color(0xFF1E2D4A)
val DarkBubbleTextOut = Color(0xFFFFFFFF)
val DarkBubbleTextIn = Color(0xFFF1F5F9)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)
val LightOutgoingBubble = Color(0xFFE0F2FE)
val LightIncomingBubble = Color(0xFFFFFFFF)
val LightBubbleTextOut = Color(0xFF0369A1)
val LightBubbleTextIn = Color(0xFF0F172A)

// Status & Accents
val OnlineGreen = Color(0xFF10B981)
val OfflineGray = Color(0xFF94A3B8)
val ReadCyan = Color(0xFF00E5FF)
val StarYellow = Color(0xFFFBBF24)
val DangerRed = Color(0xFFEF4444)
val WarningOrange = Color(0xFFF97316)
val RoyalPurple = Color(0xFF8B5CF6)
val VerifiedBadge = Color(0xFF38BDF8)

// Theme Palette Presets
enum class PaletteTheme(val displayName: String, val primary: Color, val secondary: Color, val outgoingBubbleDark: Color, val accentColor: Color) {
    CYBER_CYAN("Cyber Cyan", Color(0xFF00E5FF), Color(0xFF38BDF8), Color(0xFF0369A1), Color(0xFF00E5FF)),
    MIDNIGHT_STEALTH("Midnight Stealth", Color(0xFF10B981), Color(0xFF34D399), Color(0xFF065F46), Color(0xFF10B981)),
    ROYAL_NAVY("Royal Navy", Color(0xFF60A5FA), Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF60A5FA)),
    SOLAR_FLAME("Solar Flame", Color(0xFFFB923C), Color(0xFFF97316), Color(0xFFC2410C), Color(0xFFFB923C)),
    NEON_PURPLE("Neon Purple", Color(0xFFA78BFA), Color(0xFF8B5CF6), Color(0xFF6D28D9), Color(0xFFA78BFA)),
    CLASSIC_MESSENGER("Classic WhatsApp", Color(0xFF25D366), Color(0xFF128C7E), Color(0xFF075E54), Color(0xFF25D366))
}
