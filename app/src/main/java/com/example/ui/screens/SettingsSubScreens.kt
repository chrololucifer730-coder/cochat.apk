package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

// -------------------------------------------------------------------------
// Profile Edit Screen
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(viewModel: ChatViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var nameInput by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var bioInput by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
    var statusInput by remember(currentUser) { mutableStateOf(currentUser?.statusMessage ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(name = nameInput.ifBlank { "User" }, size = 96.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "@${currentUser?.username}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            Text(text = "Role: ${currentUser?.role?.displayName}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = statusInput,
                onValueChange = { statusInput = it },
                label = { Text("Status Message (e.g. Gaming 🎮)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bioInput,
                onValueChange = { bioInput = it },
                label = { Text("About / Bio") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    viewModel.repository.updateProfile(
                        displayName = nameInput.trim(),
                        bio = bioInput.trim(),
                        statusMessage = statusInput.trim(),
                        avatarUrl = currentUser?.avatarUrl ?: ""
                    )
                    viewModel.showToastNotification("Profile updated successfully!")
                    viewModel.navigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------------
// Privacy Settings Screen
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(viewModel: ChatViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val privacy = currentUser?.privacySettings ?: PrivacySettings()

    var lastSeen by remember(privacy) { mutableStateOf(privacy.lastSeenVisibility) }
    var onlineStatus by remember(privacy) { mutableStateOf(privacy.onlineStatusVisibility) }
    var profilePhoto by remember(privacy) { mutableStateOf(privacy.profilePhotoVisibility) }
    var statusUpdates by remember(privacy) { mutableStateOf(privacy.statusVisibility) }
    var readReceipts by remember(privacy) { mutableStateOf(privacy.readReceipts) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Privacy Controls", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text("Who can see my personal info", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                PrivacyOptionRow("Last Seen", lastSeen.label) {
                    lastSeen = if (lastSeen == VisibilityOption.EVERYONE) VisibilityOption.MY_CONTACTS else if (lastSeen == VisibilityOption.MY_CONTACTS) VisibilityOption.NOBODY else VisibilityOption.EVERYONE
                    viewModel.repository.updatePrivacySettings(privacy.copy(lastSeenVisibility = lastSeen))
                }
                PrivacyOptionRow("Online Status", onlineStatus.label) {
                    onlineStatus = if (onlineStatus == VisibilityOption.EVERYONE) VisibilityOption.MY_CONTACTS else VisibilityOption.EVERYONE
                    viewModel.repository.updatePrivacySettings(privacy.copy(onlineStatusVisibility = onlineStatus))
                }
                PrivacyOptionRow("Profile Photo", profilePhoto.label) {
                    profilePhoto = if (profilePhoto == VisibilityOption.EVERYONE) VisibilityOption.MY_CONTACTS else VisibilityOption.EVERYONE
                    viewModel.repository.updatePrivacySettings(privacy.copy(profilePhotoVisibility = profilePhoto))
                }
                PrivacyOptionRow("Status Updates", statusUpdates.label) {
                    statusUpdates = if (statusUpdates == VisibilityOption.EVERYONE) VisibilityOption.MY_CONTACTS else VisibilityOption.EVERYONE
                    viewModel.repository.updatePrivacySettings(privacy.copy(statusVisibility = statusUpdates))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Read Receipts (Blue Ticks)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("If turned off, you won't send or see read receipts.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = readReceipts,
                        onCheckedChange = {
                            readReceipts = it
                            viewModel.repository.updatePrivacySettings(privacy.copy(readReceipts = it))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyOptionRow(title: String, currentChoice: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(currentChoice, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

// -------------------------------------------------------------------------
// Account Settings Screen
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(viewModel: ChatViewModel) {
    val activeSessions by viewModel.activeSessions.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Account & Security", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text("Active Logged-In Sessions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(activeSessions) { session ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (session.deviceName.contains("Android")) Icons.Default.PhoneAndroid else Icons.Default.Computer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = session.deviceName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "${session.ipAddress} • ${session.location}", fontSize = 11.5.sp, color = Color.Gray)
                        }
                        if (session.isCurrentDevice) {
                            Surface(shape = RoundedCornerShape(4.dp), color = OnlineGreen.copy(alpha = 0.2f)) {
                                Text("This device", fontSize = 10.sp, color = OnlineGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.repository.logoutAllSessions()
                        viewModel.showToastNotification("Logged out from all other sessions")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Out from All Devices")
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, null, tint = DangerRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete My Account")
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account Permanently?") },
            text = { Text("This will permanently remove your account, messages, and profile from Friends Chat. This action cannot be undone.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.repository.deleteAccount()
                    }
                ) { Text("Confirm Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -------------------------------------------------------------------------
// Bug Report Screen
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(viewModel: ChatViewModel) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("UI / Display") }
    var severity by remember { mutableStateOf(BugSeverity.MEDIUM) }
    var steps by remember { mutableStateOf("") }

    val categories = listOf("UI / Display", "Messaging / Delivery", "Groups / Admins", "Status Stories", "Audio / Video Calls", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Report a Bug", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text("Help us make Friends Chat flawless by describing the issue:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bug Title / Summary") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, fontSize = 10.5.sp) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, fontSize = 10.5.sp) })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Severity:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BugSeverity.values().forEach { sev ->
                        FilterChip(
                            selected = severity == sev,
                            onClick = { severity = sev },
                            label = { Text(sev.label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Steps to Reproduce") },
                    placeholder = { Text("1. Tap group info\n2. Open theme settings...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic auto-attach card
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Auto-attached Device Diagnostics:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Model: Pixel 8 Pro • OS: Android 14 • App: 1.0.0 (AAB Google Play)", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    enabled = title.isNotBlank() && steps.isNotBlank(),
                    onClick = {
                        val report = viewModel.repository.submitBugReport(
                            category = category,
                            title = title.trim(),
                            steps = steps.trim(),
                            severity = severity
                        )
                        viewModel.showToastNotification("Bug report ${report.id} submitted!")
                        viewModel.navigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Submit Bug Report")
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Feedback Screen
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(viewModel: ChatViewModel) {
    var rating by remember { mutableStateOf(5) }
    var selectedCat by remember { mutableStateOf(FeedbackCategory.FEATURE_REQUEST) }
    var comments by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Send Feedback", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("How is your experience with Friends Chat?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // 5 Star Rating Bar
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $star",
                            tint = if (star <= rating) Color(0xFFFBBF24) else Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Feedback Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(FeedbackCategory.FEATURE_REQUEST, FeedbackCategory.UI_UX, FeedbackCategory.PERFORMANCE).forEach { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { selectedCat = cat },
                        label = { Text(cat.label.split(" ").first(), fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("Tell us your thoughts...") },
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = comments.isNotBlank(),
                onClick = {
                    viewModel.repository.submitFeedback(
                        rating = rating,
                        category = selectedCat,
                        comments = comments.trim()
                    )
                    viewModel.showToastNotification("Thank you for your feedback!")
                    viewModel.navigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Submit Feedback")
            }
        }
    }
}

// -------------------------------------------------------------------------
// Contact Discovery & Invite Friends Screen
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDiscoveryScreen(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val allUsers by viewModel.users.collectAsState()

    val inviteText = "Hey! Join me on Friends Chat - the fast, modern private messenger for friend squads. Download here: https://friendschat.app/download"

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Invite Friends", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Connect Your Squad", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Invite your contacts to Friends Chat using standard sharing, SMS, or WhatsApp.",
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, inviteText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Invite"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Link")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.showToastNotification("Invite link copied to clipboard!")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Contacts on Friends Chat (${allUsers.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }

            items(allUsers.take(15)) { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    UserAvatar(name = user.displayName, isOnline = user.isOnline, size = 42.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = user.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                        Text(text = "@${user.username}", fontSize = 11.5.sp, color = Color.Gray)
                    }
                    Text(text = "Joined", fontSize = 12.sp, color = OnlineGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// View Status Screen (24-hour Story Viewer)
// -------------------------------------------------------------------------
@Composable
fun StatusViewerScreen(viewModel: ChatViewModel, statusId: String) {
    val statuses by viewModel.statusItems.collectAsState()
    val status = remember(statuses, statusId) { statuses.find { it.id == statusId } }
    val currentUser by viewModel.currentUser.collectAsState()

    var progress by remember { mutableFloatStateOf(0f) }
    var replyText by remember { mutableStateOf("") }
    var showViewersSheet by remember { mutableStateOf(false) }

    LaunchedEffect(statusId) {
        viewModel.repository.recordStatusView(statusId)
        val startTime = System.currentTimeMillis()
        val durationMs = 5000L
        while (progress < 1f) {
            val elapsed = System.currentTimeMillis() - startTime
            progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            delay(50)
        }
        viewModel.navigateBack()
    }

    if (status == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Status expired or not found")
        }
        return
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(50), label = "prog")
    val isMyStatus = status.userId == currentUser?.id

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(android.graphics.Color.parseColor(status.backgroundHex)))
    ) {
        // Top Story Progress Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(name = status.userName, size = 38.dp, showOnlineBadge = false)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = status.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "Today", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        // Status Content Center
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (status.contentType == StatusContentType.TEXT) status.textContent else status.caption.ifBlank { "Photo Status" },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 34.sp
            )
        }

        // Bottom Bar: Reply or Viewers count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMyStatus) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { showViewersSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${status.viewers.size} views", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                TextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to ${status.userName}...", color = Color.White.copy(alpha = 0.6f)) },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            viewModel.repository.sendMessage(
                                chatId = "dm_${status.userId}",
                                content = "Replied to status: \"${status.textContent.take(20)}\" -> $replyText"
                            )
                            viewModel.showToastNotification("Reply sent to ${status.userName}!")
                            replyText = ""
                            viewModel.navigateBack()
                        }
                    },
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showViewersSheet) {
        AlertDialog(
            onDismissRequest = { showViewersSheet = false },
            title = { Text("Viewed By (${status.viewers.size})") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    if (status.viewers.isEmpty()) {
                        item { Text("No views yet", color = Color.Gray) }
                    }
                    items(status.viewers) { v ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            UserAvatar(name = v.userName, size = 30.dp, showOnlineBadge = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(v.userName, fontSize = 13.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewersSheet = false }) { Text("Close") }
            }
        )
    }
}
