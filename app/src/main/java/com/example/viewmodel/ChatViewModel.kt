package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.model.*
import com.example.ui.theme.ChatThemeCustomization
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AppScreen {
    object Auth : AppScreen
    object Home : AppScreen
    data class ChatConversation(val chatId: String) : AppScreen
    data class GroupDetails(val groupId: String) : AppScreen
    data class ActiveCall(val remoteUserId: String, val callType: CallType) : AppScreen
    object ThemeCustomizer : AppScreen
    object ModeratorDashboard : AppScreen
    object BugReportScreen : AppScreen
    object FeedbackScreen : AppScreen
    object ProfileEdit : AppScreen
    object PrivacySettingsScreen : AppScreen
    object AccountSettings : AppScreen
    object InviteFriends : AppScreen
    data class ViewStatus(val statusId: String) : AppScreen
}

enum class HomeTab {
    CHATS, STATUS, GROUPS, CALLS, SETTINGS
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ChatRepository(application.applicationContext)

    val currentUser = repository.currentUser
    val users = repository.users
    val chats = repository.chats
    val statusItems = repository.statusItems
    val callRecords = repository.callRecords
    val bugReports = repository.bugReports
    val feedbackItems = repository.feedbackItems
    val reports = repository.reports
    val auditLogs = repository.auditLogs
    val recoveryRequests = repository.recoveryRequests
    val activeSessions = repository.activeSessions
    val themeConfig = repository.themeConfig
    val isOfflineMode = repository.isOfflineMode
    val groupInvites = repository.groupInvites
    val joinRequests = repository.joinRequests

    // Navigation State
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentHomeTab = MutableStateFlow(HomeTab.CHATS)
    val currentHomeTab: StateFlow<HomeTab> = _currentHomeTab.asStateFlow()

    // Active Chat & Messages
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    val activeChat: StateFlow<Chat?> = combine(chats, _activeChatId) { chatList, id ->
        chatList.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMessages: StateFlow<List<Message>> = combine(repository.messages, _activeChatId) { map, id ->
        if (id != null) map[id] ?: emptyList() else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Call UI State
    data class CallState(
        val isActive: Boolean = false,
        val isConnected: Boolean = false,
        val durationSeconds: Int = 0,
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = false,
        val isVideoEnabled: Boolean = true,
        val isFrontCamera: Boolean = true,
        val remoteUser: User? = null,
        val callType: CallType = CallType.VOICE
    )

    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    private var callTimerJob: Job? = null

    // In-App Notification Toast Event
    private val _notificationEvent = MutableSharedFlow<String>()
    val notificationEvent = _notificationEvent.asSharedFlow()

    init {
        // Observe login status
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _currentScreen.value = AppScreen.Auth
                } else if (_currentScreen.value is AppScreen.Auth) {
                    _currentScreen.value = AppScreen.Home
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen is AppScreen.ChatConversation) {
            _activeChatId.value = screen.chatId
            // Mark unread as 0
            clearChatUnread(screen.chatId)
        }
        _currentScreen.value = screen
    }

    fun navigateBack() {
        val curr = _currentScreen.value
        when (curr) {
            is AppScreen.ChatConversation,
            is AppScreen.ThemeCustomizer,
            is AppScreen.ModeratorDashboard,
            is AppScreen.BugReportScreen,
            is AppScreen.FeedbackScreen,
            is AppScreen.ProfileEdit,
            is AppScreen.PrivacySettingsScreen,
            is AppScreen.AccountSettings,
            is AppScreen.InviteFriends,
            is AppScreen.ViewStatus -> {
                _currentScreen.value = AppScreen.Home
            }
            is AppScreen.GroupDetails -> {
                val active = _activeChatId.value
                if (active != null) {
                    _currentScreen.value = AppScreen.ChatConversation(active)
                } else {
                    _currentScreen.value = AppScreen.Home
                }
            }
            is AppScreen.ActiveCall -> {
                endCall()
            }
            else -> {}
        }
    }

    fun setHomeTab(tab: HomeTab) {
        _currentHomeTab.value = tab
    }

    private fun clearChatUnread(chatId: String) {
        val currentChats = chats.value
        val updated = currentChats.map {
            if (it.id == chatId) it.copy(unreadCount = 0) else it
        }
        // update internal list if needed
    }

    // Call Actions
    fun startCall(remoteUserId: String, type: CallType) {
        val target = users.value.find { it.id == remoteUserId }
        _callState.value = CallState(
            isActive = true,
            isConnected = false,
            durationSeconds = 0,
            remoteUser = target,
            callType = type,
            isVideoEnabled = type == CallType.VIDEO
        )
        navigateTo(AppScreen.ActiveCall(remoteUserId, type))

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            delay(1500) // ringing
            _callState.value = _callState.value.copy(isConnected = true)
            while (true) {
                delay(1000)
                _callState.value = _callState.value.copy(durationSeconds = _callState.value.durationSeconds + 1)
            }
        }
    }

    fun toggleMute() {
        _callState.value = _callState.value.copy(isMuted = !_callState.value.isMuted)
    }

    fun toggleSpeaker() {
        _callState.value = _callState.value.copy(isSpeakerOn = !_callState.value.isSpeakerOn)
    }

    fun toggleVideo() {
        _callState.value = _callState.value.copy(isVideoEnabled = !_callState.value.isVideoEnabled)
    }

    fun toggleCameraFlip() {
        _callState.value = _callState.value.copy(isFrontCamera = !_callState.value.isFrontCamera)
    }

    fun endCall() {
        val state = _callState.value
        callTimerJob?.cancel()
        if (state.remoteUser != null) {
            repository.recordCall(
                remoteUserId = state.remoteUser.id,
                remoteUserName = state.remoteUser.displayName,
                remoteAvatar = state.remoteUser.avatarUrl,
                type = state.callType,
                isIncoming = false,
                status = if (state.isConnected) CallStatus.ANSWERED else CallStatus.MISSED,
                durationSec = state.durationSeconds
            )
        }
        _callState.value = CallState()
        _currentScreen.value = AppScreen.Home
    }

    // Messaging
    fun sendMessage(
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        mediaSizeText: String = "",
        mediaDurationSec: Int = 0,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String = "",
        contactName: String = "",
        contactPhone: String = "",
        pollQuestion: String = "",
        pollOptions: List<String> = emptyList(),
        replyToMessageId: String? = null,
        replyToContent: String? = null,
        replyToSender: String? = null
    ) {
        val cid = _activeChatId.value ?: return
        repository.sendMessage(
            chatId = cid,
            content = content,
            type = type,
            mediaUrl = mediaUrl,
            mediaSizeText = mediaSizeText,
            mediaDurationSec = mediaDurationSec,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            contactName = contactName,
            contactPhone = contactPhone,
            pollQuestion = pollQuestion,
            pollOptions = pollOptions,
            replyToMessageId = replyToMessageId,
            replyToContent = replyToContent,
            replyToSender = replyToSender
        )
    }

    fun showToastNotification(message: String) {
        viewModelScope.launch {
            _notificationEvent.emit(message)
        }
    }
}
