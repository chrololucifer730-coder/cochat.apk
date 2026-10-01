package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val chatViewModel: ChatViewModel = viewModel()
            val themeConfig by chatViewModel.themeConfig.collectAsState()
            val currentScreen by chatViewModel.currentScreen.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            // Collect notification toast events
            LaunchedEffect(chatViewModel) {
                chatViewModel.notificationEvent.collectLatest { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }

            // System Back Handler
            BackHandler(enabled = currentScreen !is AppScreen.Home && currentScreen !is AppScreen.Auth) {
                chatViewModel.navigateBack()
            }

            MyApplicationTheme(customization = themeConfig) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val screen = currentScreen) {
                            is AppScreen.Auth -> AuthScreen(chatViewModel)
                            is AppScreen.Home -> HomeScreen(chatViewModel)
                            is AppScreen.ChatConversation -> ChatConversationScreen(chatViewModel, screen.chatId)
                            is AppScreen.GroupDetails -> GroupDetailsScreen(chatViewModel, screen.groupId)
                            is AppScreen.ActiveCall -> CallingScreen(chatViewModel)
                            is AppScreen.ThemeCustomizer -> ThemeCustomizerScreen(chatViewModel)
                            is AppScreen.ModeratorDashboard -> ModeratorDashboardScreen(chatViewModel)
                            is AppScreen.BugReportScreen -> BugReportScreen(chatViewModel)
                            is AppScreen.FeedbackScreen -> FeedbackScreen(chatViewModel)
                            is AppScreen.ProfileEdit -> ProfileEditScreen(chatViewModel)
                            is AppScreen.PrivacySettingsScreen -> PrivacySettingsScreen(chatViewModel)
                            is AppScreen.AccountSettings -> AccountSettingsScreen(chatViewModel)
                            is AppScreen.InviteFriends -> ContactDiscoveryScreen(chatViewModel)
                            is AppScreen.ViewStatus -> StatusViewerScreen(chatViewModel, screen.statusId)
                        }
                    }
                }
            }
        }
    }
}
