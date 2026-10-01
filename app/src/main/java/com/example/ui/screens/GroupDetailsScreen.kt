package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DangerRed
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(viewModel: ChatViewModel, groupId: String) {
    val chats by viewModel.chats.collectAsState()
    val group = remember(chats, groupId) { chats.find { it.id == groupId } }
    val currentUser by viewModel.currentUser.collectAsState()
    val invites by viewModel.groupInvites.collectAsState()
    val groupInvites = remember(invites, groupId) { invites.filter { it.groupId == groupId && !it.isRevoked } }
    val joinRequests by viewModel.joinRequests.collectAsState()
    val pendingRequests = remember(joinRequests, groupId) { joinRequests.filter { it.groupId == groupId && it.status == JoinRequestStatus.PENDING } }

    var selectedMemberForAction by remember { mutableStateOf<GroupMember?>(null) }
    var showSuspendDialog by remember { mutableStateOf<GroupMember?>(null) }
    var showPermissionsDialog by remember { mutableStateOf<GroupMember?>(null) }

    if (group == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Group not found")
        }
        return
    }

    val myMember = remember(group, currentUser) { group.members.find { it.userId == currentUser?.id } }
    val isCreator = myMember?.rank == AdminRank.CREATOR
    val isAdmin = isCreator || myMember?.rank == AdminRank.SENIOR_ADMIN || myMember?.rank == AdminRank.JUNIOR_ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Group Information", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(name = group.name, isGroup = true, size = 80.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = group.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(
                        text = "${group.groupType.label} • ${group.members.size} members",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (group.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = group.description,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
            }

            // Pending Join Requests
            if (isAdmin && pendingRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Join Requests (${pendingRequests.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(pendingRequests) { req ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        UserAvatar(name = req.userName, size = 36.dp, showOnlineBadge = false)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = req.userName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(text = "Requested to join", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.repository.handleJoinRequest(req.id, true) }) {
                            Icon(Icons.Default.Check, "Approve", tint = Color(0xFF10B981))
                        }
                        IconButton(onClick = { viewModel.repository.handleJoinRequest(req.id, false) }) {
                            Icon(Icons.Default.Close, "Reject", tint = DangerRed)
                        }
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
            }

            // Group Invites & Admin Actions
            if (isAdmin) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val inv = viewModel.repository.createInviteLink(groupId)
                                viewModel.showToastNotification("Invite Link Generated: https://friendschat.app/join/${inv.code}")
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Invite via Link / QR Code", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = if (groupInvites.isNotEmpty()) "Active link: ${groupInvites.first().code}" else "Generate new join link",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }

            // Members Header
            item {
                Text(
                    text = "Members (${group.members.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Members List with Badges & Permissions
            items(group.members) { member ->
                val canManageThisMember = (isCreator && member.userId != currentUser?.id) ||
                    (isAdmin && member.rank.level < (myMember?.rank?.level ?: 0))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = canManageThisMember) { selectedMemberForAction = member }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    UserAvatar(name = member.displayName, size = 44.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.displayName + if (member.userId == currentUser?.id) " (You)" else "",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp
                            )
                            if (member.isSuspended) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = DangerRed.copy(alpha = 0.2f)) {
                                    Text("Suspended", fontSize = 10.sp, color = DangerRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text(text = "@${member.username}", fontSize = 11.5.sp, color = Color.Gray)
                    }

                    // Rank badge
                    if (member.rank != AdminRank.MEMBER) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (member.rank == AdminRank.CREATOR) Color(0xFFFBBF24).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (member.rank == AdminRank.CREATOR) "👑 Creator" else member.rank.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (member.rank == AdminRank.CREATOR) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Leave / Report Group
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.repository.removeGroupMember(groupId, currentUser?.id ?: "")
                            viewModel.navigateBack()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Leave", tint = DangerRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Exit Group", color = DangerRed, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.repository.submitReport(
                                type = ReportTargetType.GROUP,
                                targetId = groupId,
                                targetTitle = group.name,
                                category = ReportCategory.SPAM,
                                description = "Reported by member"
                            )
                            viewModel.showToastNotification("Group reported to moderation team")
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Report, contentDescription = "Report", tint = DangerRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Report Group", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Member Action Options Dialog (Promote, Suspend, Remove)
    if (selectedMemberForAction != null) {
        val target = selectedMemberForAction!!
        AlertDialog(
            onDismissRequest = { selectedMemberForAction = null },
            title = { Text("Manage ${target.displayName}") },
            text = {
                Column {
                    Text("Current rank: ${target.rank.title}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (target.isSuspended) {
                        Button(
                            onClick = {
                                viewModel.repository.restoreAdminPrivileges(groupId, target.userId)
                                selectedMemberForAction = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Restore Privileges") }
                    } else {
                        Button(
                            onClick = {
                                showPermissionsDialog = target
                                selectedMemberForAction = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Change Admin Rank & Permissions") }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                showSuspendDialog = target
                                selectedMemberForAction = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Temporarily Suspend Admin") }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        onClick = {
                            viewModel.repository.removeGroupMember(groupId, target.userId)
                            selectedMemberForAction = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Remove from Group") }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberForAction = null }) { Text("Close") }
            }
        )
    }

    // Suspend Admin Dialog
    if (showSuspendDialog != null) {
        val target = showSuspendDialog!!
        var durationChoice by remember { mutableStateOf("1 Hour") }

        AlertDialog(
            onDismissRequest = { showSuspendDialog = null },
            title = { Text("Suspend Admin: ${target.displayName}") },
            text = {
                Column {
                    Text("Select suspension duration (privileges auto-restore after expiration):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("10 Minutes", "1 Hour", "24 Hours", "7 Days", "Permanent").forEach { dur ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { durationChoice = dur }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = durationChoice == dur, onClick = { durationChoice = dur })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dur, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    onClick = {
                        val minutes = when (durationChoice) {
                            "10 Minutes" -> 10L
                            "1 Hour" -> 60L
                            "24 Hours" -> 1440L
                            "7 Days" -> 10080L
                            else -> 99999999L
                        }
                        viewModel.repository.suspendAdminPrivileges(
                            groupId = groupId,
                            targetUserId = target.userId,
                            durationMinutes = minutes,
                            isPermanent = durationChoice == "Permanent",
                            reason = "Creator suspension action"
                        )
                        showSuspendDialog = null
                    }
                ) { Text("Suspend") }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Granular Permissions Dialog
    if (showPermissionsDialog != null) {
        val target = showPermissionsDialog!!
        var chosenRank by remember { mutableStateOf(target.rank) }
        val perms = remember { mutableStateListOf(*target.permissions.toTypedArray()) }

        AlertDialog(
            onDismissRequest = { showPermissionsDialog = null },
            title = { Text("Permissions: ${target.displayName}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                    Text("Select Rank:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(AdminRank.SENIOR_ADMIN, AdminRank.JUNIOR_ADMIN, AdminRank.MEMBER).forEach { r ->
                            FilterChip(
                                selected = chosenRank == r,
                                onClick = { chosenRank = r },
                                label = { Text(r.title.split(" ").first(), fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Granular Permissions:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        items(GroupPermission.values()) { p ->
                            val isChecked = perms.contains(p)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) perms.remove(p) else perms.add(p)
                                    }
                                    .padding(vertical = 3.dp)
                            ) {
                                Checkbox(checked = isChecked, onCheckedChange = {
                                    if (it) perms.add(p) else perms.remove(p)
                                })
                                Text(p.label, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.repository.updateGroupAdminRank(
                            groupId = groupId,
                            targetUserId = target.userId,
                            newRank = chosenRank,
                            permissions = perms.toSet()
                        )
                        showPermissionsDialog = null
                    }
                ) { Text("Save Permissions") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionsDialog = null }) { Text("Cancel") }
            }
        )
    }
}
