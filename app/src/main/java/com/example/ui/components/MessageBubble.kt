package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    isGroupChat: Boolean,
    theme: ChatThemeCustomization,
    onLongClick: () -> Unit,
    onVotePoll: (optionId: String) -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    // Bubble Shape according to theme
    val bubbleShape = when (theme.bubbleShape) {
        BubbleShapeType.ROUNDED_SLOPE -> {
            if (isOutgoing) RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp)
            else RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
        }
        BubbleShapeType.SLEEK_GEOMETRIC -> {
            if (isOutgoing) RoundedCornerShape(12.dp, 2.dp, 12.dp, 12.dp)
            else RoundedCornerShape(2.dp, 12.dp, 12.dp, 12.dp)
        }
        BubbleShapeType.PILL_OVAL -> RoundedCornerShape(22.dp)
    }

    val bubbleBackground = if (isOutgoing) {
        if (theme.isDarkMode) theme.selectedPalette.outgoingBubbleDark
        else LightOutgoingBubble
    } else {
        if (theme.isDarkMode) DarkIncomingBubble
        else LightIncomingBubble
    }

    val textColor = if (isOutgoing) {
        if (theme.isDarkMode) DarkBubbleTextOut else LightBubbleTextOut
    } else {
        if (theme.isDarkMode) DarkBubbleTextIn else LightBubbleTextIn
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(bubbleShape)
                .background(bubbleBackground)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Group sender name if incoming
            if (isGroupChat && !isOutgoing) {
                Text(
                    text = message.senderName,
                    color = theme.selectedPalette.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // Reply quote preview if present
            if (message.replyToContent != null) {
                Surface(
                    color = if (theme.isDarkMode) Color(0x33000000) else Color(0x1A000000),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(modifier = Modifier.padding(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(theme.selectedPalette.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = message.replyToSender ?: "Reply",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.selectedPalette.primary
                            )
                            Text(
                                text = message.replyToContent,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Message Body according to type
            when (message.type) {
                MessageType.TEXT -> {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp
                    )
                }

                MessageType.IMAGE -> {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Photo",
                                    tint = theme.selectedPalette.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "Tap to view photo",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        if (message.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = message.content, color = textColor, fontSize = 14.sp)
                        }
                    }
                }

                MessageType.AUDIO -> {
                    var isPlaying by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(theme.selectedPalette.primary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play voice note",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            // Waveform bars simulation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val barHeights = listOf(14, 22, 10, 26, 18, 12, 28, 20, 16, 24, 8, 18, 22, 14, 20)
                                barHeights.forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(h.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isPlaying) theme.selectedPalette.primary else textColor.copy(alpha = 0.5f))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "0:${message.mediaDurationSec.toString().padStart(2, '0')}",
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                MessageType.DOCUMENT -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.selectedPalette.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "File",
                                tint = theme.selectedPalette.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.content.ifBlank { "Document File" },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                                maxLines = 1
                            )
                            Text(
                                text = message.mediaSizeText.ifBlank { "1.8 MB (PDF)" },
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                MessageType.LOCATION -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F2B48)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location Pin",
                                    tint = DangerRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = message.locationName.ifBlank { "Shared GPS Location" },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                MessageType.CONTACT -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        UserAvatar(name = message.contactName.ifBlank { "Contact" }, size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.contactName.ifBlank { "Friend Contact" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = message.contactPhone.ifBlank { "+1 (555) 019-2834" },
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Contact",
                            tint = theme.selectedPalette.primary
                        )
                    }
                }

                MessageType.POLL -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Poll,
                                contentDescription = "Poll",
                                tint = theme.selectedPalette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.pollQuestion,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val totalVotes = message.pollOptions.sumOf { it.votes.size }.coerceAtLeast(1)
                        message.pollOptions.forEach { opt ->
                            val pct = (opt.votes.size.toFloat() / totalVotes) * 100f
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (theme.isDarkMode) Color(0x33000000) else Color(0x14000000),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .combinedClickable { onVotePoll(opt.id) }
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(pct / 100f)
                                            .background(theme.selectedPalette.primary.copy(alpha = 0.25f))
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = opt.text, fontSize = 13.sp, color = textColor)
                                        Text(
                                            text = "${pct.toInt()}% (${opt.votes.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text(text = message.content, color = textColor, fontSize = 14.5.sp)
                }
            }

            // Message footer: Starred icon, Edited tag, Time, and Status Ticks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (message.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (message.isStarred) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = StarYellow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (message.isEdited) {
                    Text(
                        text = "edited",
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Text(
                    text = formattedTime,
                    fontSize = 10.5.sp,
                    color = textColor.copy(alpha = 0.65f)
                )

                if (isOutgoing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageStatus.SENDING -> {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Pending delivery",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        MessageStatus.SENT -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = textColor.copy(alpha = 0.65f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        MessageStatus.DELIVERED -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = textColor.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        MessageStatus.READ -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = ReadCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Reactions bar pills
            if (message.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    message.reactions.forEach { (emoji, userIds) ->
                        Surface(
                            shape = CircleShape,
                            color = if (theme.isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.selectedPalette.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = emoji, fontSize = 12.sp)
                                if (userIds.size > 1) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${userIds.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
