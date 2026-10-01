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
import com.example.ui.theme.DangerRed
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboardScreen(viewModel: ChatViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Bug Reports", "Reports", "Feedback", "Support Recovery", "Audit Logs")

    val bugReports by viewModel.bugReports.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val feedbackItems by viewModel.feedbackItems.collectAsState()
    val recoveryRequests by viewModel.recoveryRequests.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showActionBugDialog by remember { mutableStateOf<BugReport?>(null) }
    var showActionReportDialog by remember { mutableStateOf<ReportItem?>(null) }
    var showActionRecoveryDialog by remember { mutableStateOf<AccountRecoveryRequest?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text("Moderation Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${currentUser?.displayName} (${currentUser?.role?.displayName})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                // Bug Reports
                0 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        if (bugReports.isEmpty()) {
                            item { Text("No bug reports logged.", color = Color.Gray) }
                        }
                        items(bugReports) { bug ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { showActionBugDialog = bug }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = bug.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (bug.severity) {
                                                BugSeverity.CRITICAL -> DangerRed.copy(alpha = 0.2f)
                                                BugSeverity.HIGH -> Color(0xFFF97316).copy(alpha = 0.2f)
                                                else -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = "${bug.severity.label} Severity",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = bug.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "Category: ${bug.category}", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = bug.stepsToReproduce, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Device: ${bug.deviceModel} • ${bug.androidVersion} • ${bug.appVersion}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Status: ${bug.status.label} ${if (bug.moderatorNotes.isNotBlank()) "• Note: ${bug.moderatorNotes}" else ""}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (bug.status == BugStatus.RESOLVED) OnlineGreen else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Reports (Users / Messages / Groups)
                1 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        if (reports.isEmpty()) {
                            item { Text("No active violation reports.", color = Color.Gray) }
                        }
                        items(reports) { rep ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { showActionReportDialog = rep }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "${rep.reportedType.name} REPORT", fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 12.sp)
                                        Text(text = rep.status.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Target: ${rep.reportedTitle}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Reason: ${rep.category.label}", fontSize = 12.5.sp, color = Color.Gray)
                                    Text(text = rep.description, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Feedback
                2 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        items(feedbackItems) { fb ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = fb.category.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "⭐ ${fb.rating}/5", fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = fb.comments, fontSize = 13.5.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Status: ${fb.status.label}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Support Recovery Requests
                3 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        items(recoveryRequests) { rec ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { showActionRecoveryDialog = rec }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = rec.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(text = rec.status.label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "User: @${rec.username} (${rec.email})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(text = "Reason: ${rec.reason}", fontSize = 13.sp)
                                    Text(text = "Recovery Code: ${rec.recoveryCode}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Audit Logs
                4 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        items(auditLogs) { log ->
                            val timeStr = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(text = timeStr, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(text = "Mod: ${log.moderatorName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Target: [${log.targetType}] ${log.targetId}", fontSize = 11.5.sp, color = Color.Gray)
                                    Text(text = "Reason: ${log.reason}", fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bug Dialog
    if (showActionBugDialog != null) {
        val bug = showActionBugDialog!!
        var noteInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showActionBugDialog = null },
            title = { Text("Update Bug: ${bug.id}") },
            text = {
                Column {
                    Text(bug.title, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Moderator Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = {
                            viewModel.repository.updateBugStatus(bug.id, BugStatus.IN_PROGRESS, noteInput)
                            showActionBugDialog = null
                        }) { Text("In Progress") }
                        Button(onClick = {
                            viewModel.repository.updateBugStatus(bug.id, BugStatus.RESOLVED, noteInput)
                            showActionBugDialog = null
                        }) { Text("Resolve") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionBugDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Report Action Dialog
    if (showActionReportDialog != null) {
        val rep = showActionReportDialog!!
        AlertDialog(
            onDismissRequest = { showActionReportDialog = null },
            title = { Text("Take Action on Report") },
            text = {
                Column {
                    Text(rep.reportedTitle, fontWeight = FontWeight.Bold)
                    Text("Category: ${rep.category.label}", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        onClick = {
                            viewModel.repository.handleReportAction(rep.id, ReportStatus.ACTION_TAKEN, banTarget = true, notes = "Violated safety policy")
                            showActionReportDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Ban / Suspend Account") }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.repository.handleReportAction(rep.id, ReportStatus.DISMISSED, banTarget = false, notes = "No violation found")
                            showActionReportDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Dismiss Report") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionReportDialog = null }) { Text("Close") }
            }
        )
    }

    // Recovery Action Dialog
    if (showActionRecoveryDialog != null) {
        val rec = showActionRecoveryDialog!!
        var tempPass by remember { mutableStateOf("tempPass123!") }
        AlertDialog(
            onDismissRequest = { showActionRecoveryDialog = null },
            title = { Text("Resolve Recovery Request") },
            text = {
                Column {
                    Text("User: @${rec.username}", fontWeight = FontWeight.Bold)
                    Text("Email: ${rec.email}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempPass,
                        onValueChange = { tempPass = it },
                        label = { Text("New Temporary Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.repository.resolveAccountRecovery(rec.id, tempPass, RecoveryStatus.RESOLVED)
                    showActionRecoveryDialog = null
                    viewModel.showToastNotification("Recovery resolved! New password set.")
                }) { Text("Restore Access") }
            },
            dismissButton = {
                TextButton(onClick = { showActionRecoveryDialog = null }) { Text("Cancel") }
            }
        )
    }
}
