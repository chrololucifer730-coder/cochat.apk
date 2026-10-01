package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserAvatar
import com.example.viewmodel.ChatViewModel

@Composable
fun AuthScreen(viewModel: ChatViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070C19), Color(0xFF0F1A30), Color(0xFF0A0F1D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(name = "Friends Chat", size = 68.dp, showOnlineBadge = false)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Friends Chat",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isRegisterMode) "Create your account" else "Sign in to connect with friends",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33EF4444),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email or Username") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Unique Username (e.g. alex_dev)") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it; errorMessage = null },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (isRegisterMode) {
                            val res = viewModel.repository.register(email, username, displayName, password)
                            if (res.isFailure) {
                                errorMessage = res.exceptionOrNull()?.message
                            }
                        } else {
                            val res = viewModel.repository.login(email, password)
                            if (res.isFailure) {
                                errorMessage = res.exceptionOrNull()?.message
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Register" else "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isRegisterMode = !isRegisterMode; errorMessage = null }) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? Sign In" else "New user? Create Account",
                            fontSize = 12.sp
                        )
                    }

                    if (!isRegisterMode) {
                        TextButton(onClick = { showRecoveryDialog = true }) {
                            Text("Forgot password?", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Account Recovery Request Dialog
    if (showRecoveryDialog) {
        var recEmail by remember { mutableStateOf("") }
        var recUser by remember { mutableStateOf("") }
        var recReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text("Account Recovery Request") },
            text = {
                Column {
                    Text("Submit your email and username for secure recovery by the support team:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recEmail,
                        onValueChange = { recEmail = it },
                        label = { Text("Account Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = recUser,
                        onValueChange = { recUser = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = recReason,
                        onValueChange = { recReason = it },
                        label = { Text("Reason for lost access") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = recEmail.isNotBlank() && recUser.isNotBlank(),
                    onClick = {
                        val req = viewModel.repository.submitAccountRecovery(recEmail, recUser, recReason)
                        showRecoveryDialog = false
                        viewModel.showToastNotification("Recovery ticket ${req.id} created! Recovery code: ${req.recoveryCode}")
                    }
                ) { Text("Submit Request") }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) { Text("Cancel") }
            }
        )
    }
}
