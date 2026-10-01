package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.BottomNavBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.ReadCyan
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.HomeTab
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ChatViewModel) {
    val currentTab by viewModel.currentHomeTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val calls by viewModel.callRecords.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val allUsers by viewModel.users.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var showNewStatusDialog by remember { mutableStateOf(false) }

    val unreadCount = remember(chats) { chats.sumOf { it.unreadCount } }
    val missedCallsCount = remember(calls) { calls.count { it.status == CallStatus.MISSED } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search chats, messages, people...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Friends Chat",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Simulation Indicator Chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isOffline) Color(0xFFF59E0B).copy(alpha = 0.2f) else OnlineGreen.copy(alpha = 0.2f),
                                modifier = Modifier.clickable {
                                    viewModel.repository.setOfflineMode(!isOffline)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isOffline) Color(0xFFF59E0B) else OnlineGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOffline) "Offline Queue" else "Online",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOffline) Color(0xFFF59E0B) else OnlineGreen
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearching = !isSearching
                        if (!isSearching) searchQuery = ""
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    // Account switch menu (great for demonstrating 50 users & multi-user chat)
                    Box {
                        IconButton(onClick = { showAccountMenu = true }) {
                            UserAvatar(
                                name = currentUser?.displayName ?: "User",
                                size = 32.dp,
                                showOnlineBadge = false
                            )
                        }
                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = currentUser?.displayName ?: "", fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "@${currentUser?.username} • ${currentUser?.role?.displayName}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    showAccountMenu = false
                                    viewModel.navigateTo(AppScreen.ProfileEdit)
                                }
                            )
                            HorizontalDivider()
                            Text(
                                text = "Switch Account (50-User Demo)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            allUsers.take(6).forEach { u ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            UserAvatar(name = u.displayName, size = 26.dp, showOnlineBadge = false)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(text = u.displayName, fontSize = 13.sp)
                                                Text(text = "@${u.username}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    },
                                    onClick = {
                                        showAccountMenu = false
                                        viewModel.repository.switchAccount(u.id)
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Invite Friends", color = MaterialTheme.colorScheme.primary) },
                                leadingIcon = { Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showAccountMenu = false
                                    viewModel.navigateTo(AppScreen.InviteFriends)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null) },
                                onClick = {
                                    showAccountMenu = false
                                    viewModel.repository.logout()
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
            BottomNavBar(
                currentTab = currentTab,
                unreadMessagesCount = unreadCount,
                missedCallsCount = missedCallsCount,
                onTabSelected = { viewModel.setHomeTab(it) }
            )
        },
        floatingActionButton = {
            when (currentTab) {
                HomeTab.CHATS -> {
                    FloatingActionButton(
                        onClick = { showNewChatDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "New Chat")
                    }
                }
                HomeTab.STATUS -> {
                    FloatingActionButton(
                        onClick = { showNewStatusDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Add Status")
                    }
                }
                HomeTab.GROUPS -> {
                    FloatingActionButton(
                        onClick = { showNewGroupDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "Create Group")
                    }
                }
                HomeTab.CALLS -> {
                    FloatingActionButton(
                        onClick = { showNewChatDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Start Call")
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                HomeTab.CHATS -> ChatsTab(viewModel, searchQuery)
                HomeTab.STATUS -> StatusTab(viewModel)
                HomeTab.GROUPS -> GroupsTab(viewModel, searchQuery)
                HomeTab.CALLS -> CallsTab(viewModel)
                HomeTab.SETTINGS -> SettingsTab(viewModel)
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Select Friend to Chat") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                    val me = currentUser
                    val selectableUsers = allUsers.filter { it.id != me?.id }
                    items(selectableUsers) { friend ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNewChatDialog = false
                                    // Find or create direct chat
                                    val existingChat = chats.find { !it.isGroup && it.participantIds.contains(friend.id) }
                                    if (existingChat != null) {
                                        viewModel.navigateTo(AppScreen.ChatConversation(existingChat.id))
                                    } else {
                                        val newChat = Chat(
                                            id = "dm_${friend.id}",
                                            name = friend.displayName,
                                            isGroup = false,
                                            avatarUrl = friend.avatarUrl,
                                            participantIds = listOf(me?.id ?: "", friend.id)
                                        )
                                        viewModel.repository.sendMessage(
                                            chatId = newChat.id,
                                            content = "Hey ${friend.displayName}!",
                                            type = MessageType.TEXT
                                        )
                                        viewModel.navigateTo(AppScreen.ChatConversation(newChat.id))
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            UserAvatar(name = friend.displayName, isOnline = friend.isOnline, size = 42.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = friend.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = friend.bio, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) { Text("Cancel") }
            }
        )
    }

    // New Group Dialog
    if (showNewGroupDialog) {
        CreateGroupDialog(
            viewModel = viewModel,
            onDismiss = { showNewGroupDialog = false },
            onCreated = { groupChat ->
                showNewGroupDialog = false
                viewModel.navigateTo(AppScreen.ChatConversation(groupChat.id))
            }
        )
    }

    // New Status Dialog
    if (showNewStatusDialog) {
        CreateStatusDialog(
            viewModel = viewModel,
            onDismiss = { showNewStatusDialog = false }
        )
    }
}

// -------------------------------------------------------------------------
// Chats Tab
// -------------------------------------------------------------------------
@Composable
fun ChatsTab(viewModel: ChatViewModel, searchQuery: String) {
    val chats by viewModel.chats.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredChats = remember(chats, searchQuery, selectedFilter) {
        chats.filter { chat ->
            val matchQuery = searchQuery.isBlank() ||
                chat.name.contains(searchQuery, ignoreCase = true) ||
                (chat.lastMessage?.content?.contains(searchQuery, ignoreCase = true) == true)

            val matchFilter = when (selectedFilter) {
                "Unread" -> chat.unreadCount > 0
                "Groups" -> chat.isGroup
                "Favorites" -> chat.isFavorite
                else -> true
            }
            matchQuery && matchFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Filters Chip Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Unread", "Groups", "Favorites").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        if (filteredChats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "No chats",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No conversations found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredChats, key = { it.id }) { chat ->
                    ChatItemRow(
                        chat = chat,
                        onClick = { viewModel.navigateTo(AppScreen.ChatConversation(chat.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(chat: Chat, onClick: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(chat.updatedAt) { timeFormat.format(Date(chat.updatedAt)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        UserAvatar(
            name = chat.name,
            isGroup = chat.isGroup,
            size = 52.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formattedTime,
                    fontSize = 11.5.sp,
                    color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lastMsg = chat.lastMessage
                    if (lastMsg != null && lastMsg.status == MessageStatus.READ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = ReadCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = lastMsg?.content ?: "No messages yet",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chat.unreadCount}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Status / Updates Tab
// -------------------------------------------------------------------------
@Composable
fun StatusTab(viewModel: ChatViewModel) {
    val statuses by viewModel.statusItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val myStatuses = remember(statuses, currentUser) {
        statuses.filter { it.userId == currentUser?.id }
    }
    val friendsStatuses = remember(statuses, currentUser) {
        statuses.filter { it.userId != currentUser?.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // My Status Item
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (myStatuses.isNotEmpty()) {
                            viewModel.navigateTo(AppScreen.ViewStatus(myStatuses.first().id))
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box {
                    UserAvatar(name = currentUser?.displayName ?: "Me", size = 54.dp, showOnlineBadge = false)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "My Status", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = if (myStatuses.isEmpty()) "Tap to add status update (24h)" else "${myStatuses.size} update(s) • ${myStatuses.first().viewers.size} views",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        // Recent Updates Section
        item {
            Text(
                text = "Recent Updates",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (friendsStatuses.isEmpty()) {
            item {
                Text(
                    text = "No updates from friends yet.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(friendsStatuses) { st ->
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeStr = timeFormat.format(Date(st.timestamp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.ViewStatus(st.id)) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(name = st.userName, size = 50.dp, showOnlineBadge = false)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = st.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = if (st.contentType == StatusContentType.TEXT) st.textContent else st.caption.ifBlank { "Photo Status" },
                            fontSize = 13.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = timeStr, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Groups & Communities Tab
// -------------------------------------------------------------------------
@Composable
fun GroupsTab(viewModel: ChatViewModel, searchQuery: String) {
    val chats by viewModel.chats.collectAsState()
    val groupChats = remember(chats, searchQuery) {
        chats.filter { it.isGroup && (searchQuery.isBlank() || it.name.contains(searchQuery, true)) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Communities",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Creator & Admin Powered Groups", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Granular roles, suspensions, invite links & announcement channels.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Your Groups & Communities (${groupChats.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(groupChats) { group ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.ChatConversation(group.id)) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                UserAvatar(name = group.name, isGroup = true, size = 52.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = group.groupType.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${group.members.size} members • ${group.description}",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Calls Tab
// -------------------------------------------------------------------------
@Composable
fun CallsTab(viewModel: ChatViewModel) {
    val callRecords by viewModel.callRecords.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Calls",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = "${callRecords.size} logged", fontSize = 12.sp, color = Color.Gray)
            }
        }

        items(callRecords) { call ->
            val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val dateStr = timeFormat.format(Date(call.timestamp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                UserAvatar(name = call.remoteUserName, size = 48.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = call.remoteUserName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (call.status == CallStatus.MISSED) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (call.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                            contentDescription = null,
                            tint = if (call.status == CallStatus.MISSED) Color(0xFFEF4444) else OnlineGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$dateStr ${if (call.durationSeconds > 0) "• ${call.durationSeconds}s" else ""}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = { viewModel.startCall(call.remoteUserId, call.callType) }) {
                    Icon(
                        imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Settings Tab
// -------------------------------------------------------------------------
@Composable
fun SettingsTab(viewModel: ChatViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.ProfileEdit) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    UserAvatar(name = currentUser?.displayName ?: "User", size = 64.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "Your Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "@${currentUser?.username}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentUser?.statusMessage ?: "Available",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider()
        }

        // Settings Category List
        item {
            SettingsCategoryRow(
                icon = Icons.Default.AccountCircle,
                title = "Account",
                subtitle = "Security notifications, change password, delete account",
                onClick = { viewModel.navigateTo(AppScreen.AccountSettings) }
            )
            SettingsCategoryRow(
                icon = Icons.Default.Lock,
                title = "Privacy",
                subtitle = "Last seen, online status, profile photo, read receipts",
                onClick = { viewModel.navigateTo(AppScreen.PrivacySettingsScreen) }
            )
            SettingsCategoryRow(
                icon = Icons.Default.Palette,
                title = "Appearance & Wallpaper",
                subtitle = "Theme colors, custom wallpapers, brightness, blur",
                onClick = { viewModel.navigateTo(AppScreen.ThemeCustomizer) }
            )
            SettingsCategoryRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Message sounds, group tones, preview alerts",
                onClick = { viewModel.showToastNotification("Notification settings updated") }
            )
            SettingsCategoryRow(
                icon = Icons.Default.DataUsage,
                title = "Storage & Data",
                subtitle = "Auto-download preferences, network usage",
                onClick = { viewModel.showToastNotification("Cache cleared (14.2 MB freed)") }
            )
            SettingsCategoryRow(
                icon = Icons.Default.BugReport,
                title = "Report a Bug",
                subtitle = "Submit issue with diagnostics & device specs",
                onClick = { viewModel.navigateTo(AppScreen.BugReportScreen) }
            )
            SettingsCategoryRow(
                icon = Icons.Default.Feedback,
                title = "Send Feedback",
                subtitle = "Share suggestions, rating & feature requests",
                onClick = { viewModel.navigateTo(AppScreen.FeedbackScreen) }
            )

            // Moderation Dashboard for authorized roles
            val role = currentUser?.role ?: UserRole.USER
            if (role != UserRole.USER) {
                SettingsCategoryRow(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Moderation Dashboard",
                    subtitle = "Review bug tickets, feedback, reports & audit logs",
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.navigateTo(AppScreen.ModeratorDashboard) }
                )
            }

            SettingsCategoryRow(
                icon = Icons.Default.PersonAdd,
                title = "Invite Friends",
                subtitle = "Share download link, SMS, WhatsApp invite",
                onClick = { viewModel.navigateTo(AppScreen.InviteFriends) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.repository.logout() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SettingsCategoryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = tint)
            Text(text = subtitle, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}

// -------------------------------------------------------------------------
// Dialogs: Create Group & Create Status
// -------------------------------------------------------------------------
@Composable
fun CreateGroupDialog(viewModel: ChatViewModel, onDismiss: () -> Unit, onCreated: (Chat) -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GroupType.PRIVATE) }
    val allUsers by viewModel.users.collectAsState()
    val currUser by viewModel.currentUser.collectAsState()
    val selectedMembers = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Group") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Topic") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Group Privacy Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(GroupType.PRIVATE, GroupType.PUBLIC, GroupType.ANNOUNCEMENT).forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.label.split(" ").first(), fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Add Initial Members:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    items(allUsers.filter { it.id != currUser?.id }) { user ->
                        val isChecked = selectedMembers.contains(user.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedMembers.remove(user.id)
                                    else selectedMembers.add(user.id)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = isChecked, onCheckedChange = {
                                if (it) selectedMembers.add(user.id) else selectedMembers.remove(user.id)
                            })
                            UserAvatar(name = user.displayName, size = 30.dp, showOnlineBadge = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = user.displayName, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = groupName.isNotBlank(),
                onClick = {
                    val newGroup = viewModel.repository.createGroup(
                        name = groupName,
                        description = description,
                        type = selectedType,
                        memberUserIds = selectedMembers.toList()
                    )
                    onCreated(newGroup)
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateStatusDialog(viewModel: ChatViewModel, onDismiss: () -> Unit) {
    var statusText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#0284C7") }

    val bgColors = listOf("#0284C7", "#10B981", "#7C3AED", "#D97706", "#EF4444", "#0F172A")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Status Story (24h)") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(android.graphics.Color.parseColor(selectedColor))),
                    contentAlignment = Alignment.Center
                ) {
                    TextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        placeholder = { Text("What's on your mind?", color = Color.White.copy(alpha = 0.7f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Pick Background Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    bgColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = statusText.isNotBlank(),
                onClick = {
                    viewModel.repository.postStatus(
                        textContent = statusText,
                        backgroundHex = selectedColor,
                        contentType = StatusContentType.TEXT
                    )
                    onDismiss()
                }
            ) {
                Text("Post Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
