package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Message
import com.example.ui.theme.DangerRed
import com.example.ui.theme.StarYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionModal(
    message: Message,
    isOutgoing: Boolean,
    onDismiss: () -> Unit,
    onReaction: (emoji: String) -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onStar: () -> Unit,
    onPin: () -> Unit,
    onForward: () -> Unit,
    onDeleteForMe: () -> Unit,
    onDeleteForEveryone: () -> Unit,
    onReport: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Quick Emoji Reaction Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("👍", "❤️", "😂", "😮", "😢", "🔥").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                onReaction(emoji)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Items Grid / List
            Row(modifier = Modifier.fillMaxWidth()) {
                ActionTile(
                    icon = Icons.Default.Reply,
                    label = "Reply",
                    modifier = Modifier.weight(1f),
                    onClick = { onReply(); onDismiss() }
                )
                ActionTile(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy",
                    modifier = Modifier.weight(1f),
                    onClick = { onCopy(); onDismiss() }
                )
                ActionTile(
                    icon = Icons.Default.Star,
                    label = if (message.isStarred) "Unstar" else "Star",
                    tint = if (message.isStarred) StarYellow else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = { onStar(); onDismiss() }
                )
                ActionTile(
                    icon = Icons.Default.PushPin,
                    label = if (message.isPinned) "Unpin" else "Pin",
                    modifier = Modifier.weight(1f),
                    onClick = { onPin(); onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ActionTile(
                    icon = Icons.Default.Forward,
                    label = "Forward",
                    modifier = Modifier.weight(1f),
                    onClick = { onForward(); onDismiss() }
                )
                if (isOutgoing) {
                    ActionTile(
                        icon = Icons.Default.Edit,
                        label = "Edit",
                        modifier = Modifier.weight(1f),
                        onClick = { onEdit(); onDismiss() }
                    )
                }
                ActionTile(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    tint = DangerRed,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // Will show delete options
                        onDeleteForMe()
                        onDismiss()
                    }
                )
                if (isOutgoing) {
                    ActionTile(
                        icon = Icons.Default.DeleteForever,
                        label = "For All",
                        tint = DangerRed,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDeleteForEveryone()
                            onDismiss()
                        }
                    )
                } else {
                    ActionTile(
                        icon = Icons.Default.Report,
                        label = "Report",
                        tint = DangerRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onReport(); onDismiss() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = tint)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSendImage: () -> Unit,
    onSendAudio: () -> Unit,
    onSendDocument: () -> Unit,
    onSendLocation: () -> Unit,
    onSendContact: () -> Unit,
    onSendPoll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Share Content",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentIcon(
                    icon = Icons.Default.Image,
                    label = "Photo",
                    color = Color(0xFF0284C7),
                    onClick = { onSendImage(); onDismiss() }
                )
                AttachmentIcon(
                    icon = Icons.Default.Mic,
                    label = "Voice Note",
                    color = Color(0xFF10B981),
                    onClick = { onSendAudio(); onDismiss() }
                )
                AttachmentIcon(
                    icon = Icons.Default.InsertDriveFile,
                    label = "Document",
                    color = Color(0xFF8B5CF6),
                    onClick = { onSendDocument(); onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentIcon(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    color = Color(0xFFEF4444),
                    onClick = { onSendLocation(); onDismiss() }
                )
                AttachmentIcon(
                    icon = Icons.Default.Person,
                    label = "Contact",
                    color = Color(0xFFF59E0B),
                    onClick = { onSendContact(); onDismiss() }
                )
                AttachmentIcon(
                    icon = Icons.Default.Poll,
                    label = "Poll",
                    color = Color(0xFF06B6D4),
                    onClick = { onSendPoll(); onDismiss() }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttachmentIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
