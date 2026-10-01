package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnlineGreen

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String = "",
    isOnline: Boolean = false,
    size: Dp = 48.dp,
    showOnlineBadge: Boolean = true,
    isGroup: Boolean = false
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

    // Boyish tech gradient palettes based on name hash
    val hash = kotlin.math.abs(name.hashCode())
    val gradientColors = when (hash % 6) {
        0 -> listOf(Color(0xFF0284C7), Color(0xFF00E5FF))
        1 -> listOf(Color(0xFF059669), Color(0xFF10B981))
        2 -> listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))
        3 -> listOf(Color(0xFFD97706), Color(0xFFFBBF24))
        4 -> listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
        else -> listOf(Color(0xFF2563EB), Color(0xFF38BDF8))
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            if (isGroup) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            } else if (name.isBlank()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            } else {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }

        if (showOnlineBadge && isOnline && !isGroup) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
