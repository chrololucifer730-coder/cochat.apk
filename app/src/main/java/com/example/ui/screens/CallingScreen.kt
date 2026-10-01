package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallType
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DangerRed
import com.example.viewmodel.ChatViewModel

@Composable
fun CallingScreen(viewModel: ChatViewModel) {
    val callState by viewModel.callState.collectAsState()
    val remoteUser = callState.remoteUser

    // Pulse animation for ringing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val durationText = remember(callState.durationSeconds, callState.isConnected) {
        if (!callState.isConnected) "Ringing..."
        else {
            val mins = callState.durationSeconds / 60
            val secs = callState.durationSeconds % 60
            "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070C18))
    ) {
        // Video Preview Simulated View if video call and video is enabled
        if (callState.callType == CallType.VIDEO && callState.isVideoEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1E36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (callState.isConnected) "HD Video Feed Connected" else "Establishing video connection...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }

        // Header User Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = remoteUser?.displayName ?: "Friend",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "@${remoteUser?.username}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = CircleShape,
                color = Color(0x33FFFFFF)
            ) {
                Text(
                    text = durationText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Avatar with pulse ring if audio call or ringing
            Box(contentAlignment = Alignment.Center) {
                if (!callState.isConnected) {
                    Box(
                        modifier = Modifier
                            .size((120 * pulseScale).dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                    )
                }
                UserAvatar(
                    name = remoteUser?.displayName ?: "Friend",
                    size = 120.dp,
                    showOnlineBadge = false
                )
            }
        }

        // Floating Pip Self View if Video Call
        if (callState.callType == CallType.VIDEO && callState.isVideoEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
                    .size(90.dp, 130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Self", color = Color.White, fontSize = 11.sp)
            }
        }

        // Bottom Controls Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallControlButton(
                    icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = callState.isMuted,
                    label = if (callState.isMuted) "Unmute" else "Mute",
                    onClick = { viewModel.toggleMute() }
                )

                CallControlButton(
                    icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    isActive = callState.isSpeakerOn,
                    label = "Speaker",
                    onClick = { viewModel.toggleSpeaker() }
                )

                if (callState.callType == CallType.VIDEO) {
                    CallControlButton(
                        icon = if (callState.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        isActive = !callState.isVideoEnabled,
                        label = "Video",
                        onClick = { viewModel.toggleVideo() }
                    )
                    CallControlButton(
                        icon = Icons.Default.FlipCameraAndroid,
                        isActive = false,
                        label = "Flip",
                        onClick = { viewModel.toggleCameraFlip() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // End Call Button
            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(DangerRed)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    isActive: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0x33FFFFFF))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}
