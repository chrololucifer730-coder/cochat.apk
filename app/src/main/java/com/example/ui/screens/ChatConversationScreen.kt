package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.MessageActionModal
import com.example.ui.components.MessageBubble
import com.example.ui.components.UserAvatar
import com.example.ui.components.WallpaperContainer
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(viewModel: ChatViewModel, chatId: String) {
    val activeChat by viewModel.activeChat.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val themeConfig by viewModel.themeConfig.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var selectedMessageForAction by remember { mutableStateOf<Message?>(null) }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Message?>(null) }
    var showForwardDialog by remember { mutableStateOf<Message?>(null) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    val pinnedMessage = remember(activeMessages, activeChat) {
        val pid = activeChat?.pinnedMessageId
        if (pid != null) activeMessages.find { it.id == pid } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (activeChat?.isGroup == true) {
                                    viewModel.navigateTo(AppScreen.GroupDetails(chatId))
                                }
                            }
                    ) {
                        UserAvatar(
                            name = activeChat?.name ?: "Chat",
                            isGroup = activeChat?.isGroup == true,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activeChat?.name ?: "Chat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Text(
                                text = if (activeChat?.isGroup == true) "${activeChat?.members?.size ?: 0} members" else "Online",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val otherId = activeChat?.participantIds?.firstOrNull { it != currentUser?.id } ?: ""
                        viewModel.startCall(otherId, CallType.VIDEO)
                    }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                    IconButton(onClick = {
                        val otherId = activeChat?.participantIds?.firstOrNull { it != currentUser?.id } ?: ""
                        viewModel.startCall(otherId, CallType.VOICE)
                    }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Voice Call")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            if (activeChat?.isGroup == true) {
                                DropdownMenuItem(
                                    text = { Text("Group Info & Admin") },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.navigateTo(AppScreen.GroupDetails(chatId))
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Mute Notifications") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.showToastNotification("Chat muted for 8 hours")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Disappearing Messages (24h)") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.showToastNotification("Disappearing messages set to 24 hours")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Customize Wallpaper") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.navigateTo(AppScreen.ThemeCustomizer)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.showToastNotification("Chat cleared")
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                // Reply quote banner if active
                if (replyingToMessage != null) {
                    val rep = replyingToMessage!!
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(32.dp)
                                    .background(themeConfig.selectedPalette.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to ${rep.senderName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeConfig.selectedPalette.primary
                                )
                                Text(
                                    text = rep.content,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { replyingToMessage = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Reply", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Chat Input Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(onClick = { showAttachmentSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Message...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 4,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(
                                    content = textInput.trim(),
                                    replyToMessageId = replyingToMessage?.id,
                                    replyToContent = replyingToMessage?.content,
                                    replyToSender = replyingToMessage?.senderName
                                )
                                textInput = ""
                                replyingToMessage = null
                            } else {
                                // Send simulated quick voice note
                                viewModel.sendMessage(
                                    content = "Voice note (0:15)",
                                    type = MessageType.AUDIO,
                                    mediaDurationSec = 15
                                )
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(themeConfig.selectedPalette.primary)
                    ) {
                        Icon(
                            imageVector = if (textInput.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = "Send",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        WallpaperContainer(
            theme = themeConfig,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Pinned Message Header
                if (pinnedMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val idx = activeMessages.indexOfFirst { it.id == pinnedMessage.id }
                                if (idx >= 0) {
                                    scope.launch { listState.animateScrollToItem(idx) }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = themeConfig.selectedPalette.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pinned Message", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeConfig.selectedPalette.primary)
                                Text(pinnedMessage.content, fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Messages Stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    items(activeMessages, key = { it.id }) { msg ->
                        val isOut = msg.senderId == currentUser?.id
                        MessageBubble(
                            message = msg,
                            isOutgoing = isOut,
                            isGroupChat = activeChat?.isGroup == true,
                            theme = themeConfig,
                            onLongClick = { selectedMessageForAction = msg },
                            onVotePoll = { optId ->
                                viewModel.repository.votePoll(chatId, msg.id, optId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Long press action modal
    if (selectedMessageForAction != null) {
        val targetMsg = selectedMessageForAction!!
        MessageActionModal(
            message = targetMsg,
            isOutgoing = targetMsg.senderId == currentUser?.id,
            onDismiss = { selectedMessageForAction = null },
            onReaction = { emoji ->
                viewModel.repository.addReaction(chatId, targetMsg.id, emoji)
            },
            onReply = { replyingToMessage = targetMsg },
            onCopy = {
                viewModel.showToastNotification("Copied to clipboard")
            },
            onEdit = { showEditDialog = targetMsg },
            onStar = { viewModel.repository.toggleStarMessage(chatId, targetMsg.id) },
            onPin = { viewModel.repository.togglePinMessage(chatId, targetMsg.id) },
            onForward = { showForwardDialog = targetMsg },
            onDeleteForMe = { viewModel.repository.deleteMessageForMe(chatId, targetMsg.id) },
            onDeleteForEveryone = { viewModel.repository.deleteMessageForEveryone(chatId, targetMsg.id) },
            onReport = {
                viewModel.repository.submitReport(
                    type = ReportTargetType.MESSAGE,
                    targetId = targetMsg.id,
                    targetTitle = targetMsg.content.take(30),
                    category = ReportCategory.INAPPROPRIATE,
                    description = "Reported by user from chat"
                )
                viewModel.showToastNotification("Message reported to moderators")
            }
        )
    }

    // Attachment Sheet
    if (showAttachmentSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachmentSheet = false },
            onSendImage = {
                viewModel.sendMessage(
                    content = "Attached Photo from camera",
                    type = MessageType.IMAGE,
                    mediaUrl = "photo_shot"
                )
            },
            onSendAudio = {
                viewModel.sendMessage(
                    content = "Voice note recording (0:28)",
                    type = MessageType.AUDIO,
                    mediaDurationSec = 28
                )
            },
            onSendDocument = {
                viewModel.sendMessage(
                    content = "Project_Specs_V1.pdf",
                    type = MessageType.DOCUMENT,
                    mediaSizeText = "3.2 MB (PDF)"
                )
            },
            onSendLocation = { showLocationDialog = true },
            onSendContact = { showContactDialog = true },
            onSendPoll = { showPollDialog = true }
        )
    }

    // Poll Creator Dialog
    if (showPollDialog) {
        var question by remember { mutableStateOf("") }
        var opt1 by remember { mutableStateOf("") }
        var opt2 by remember { mutableStateOf("") }
        var opt3 by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPollDialog = false },
            title = { Text("Create Poll") },
            text = {
                Column {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Ask a question...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = opt1,
                        onValueChange = { opt1 = it },
                        label = { Text("Option 1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = opt2,
                        onValueChange = { opt2 = it },
                        label = { Text("Option 2") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = opt3,
                        onValueChange = { opt3 = it },
                        label = { Text("Option 3 (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = question.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank(),
                    onClick = {
                        val opts = listOfNotNull(opt1.takeIf { it.isNotBlank() }, opt2.takeIf { it.isNotBlank() }, opt3.takeIf { it.isNotBlank() })
                        viewModel.sendMessage(
                            content = question,
                            type = MessageType.POLL,
                            pollQuestion = question,
                            pollOptions = opts
                        )
                        showPollDialog = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showPollDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Location Sharing Dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Share Location") },
            text = {
                Text("Share your current GPS location (Downtown Tech Center, 37.7749° N, 122.4194° W)?")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendMessage(
                        content = "Current GPS Location",
                        type = MessageType.LOCATION,
                        latitude = 37.7749,
                        longitude = -122.4194,
                        locationName = "Downtown Tech Hub"
                    )
                    showLocationDialog = false
                }) { Text("Share") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Contact Sharing Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Share Contact Card") },
            text = {
                Text("Share contact card for Liam Chen (+1 (555) 019-2834)?")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.sendMessage(
                        content = "Contact Card",
                        type = MessageType.CONTACT,
                        contactName = "Liam Chen",
                        contactPhone = "+1 (555) 019-2834"
                    )
                    showContactDialog = false
                }) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Message Dialog
    if (showEditDialog != null) {
        var editContent by remember { mutableStateOf(showEditDialog!!.content) }
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = editContent.isNotBlank(),
                    onClick = {
                        viewModel.repository.editMessage(chatId, showEditDialog!!.id, editContent)
                        showEditDialog = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Forward Dialog
    if (showForwardDialog != null) {
        val allChats by viewModel.chats.collectAsState()
        AlertDialog(
            onDismissRequest = { showForwardDialog = null },
            title = { Text("Forward Message To") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    items(allChats.filter { it.id != chatId }) { ch ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.repository.forwardMessage(chatId, showForwardDialog!!.id, ch.id)
                                    showForwardDialog = null
                                    viewModel.showToastNotification("Forwarded to ${ch.name}")
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            UserAvatar(name = ch.name, isGroup = ch.isGroup, size = 34.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = ch.name, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showForwardDialog = null }) { Text("Cancel") }
            }
        )
    }
}
