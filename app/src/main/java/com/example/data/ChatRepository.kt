package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import com.example.ui.theme.BubbleShapeType
import com.example.ui.theme.ChatThemeCustomization
import com.example.ui.theme.PaletteTheme
import com.example.ui.theme.WallpaperType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class ChatRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("friends_chat_db_v1", Context.MODE_PRIVATE)

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Current User Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // All Users Directory (50 users)
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Chats List
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    // Messages map: chatId -> List<Message>
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Status Stories
    private val _statusItems = MutableStateFlow<List<StatusItem>>(emptyList())
    val statusItems: StateFlow<List<StatusItem>> = _statusItems.asStateFlow()

    // Call Logs
    private val _callRecords = MutableStateFlow<List<CallRecord>>(emptyList())
    val callRecords: StateFlow<List<CallRecord>> = _callRecords.asStateFlow()

    // Bug Reports
    private val _bugReports = MutableStateFlow<List<BugReport>>(emptyList())
    val bugReports: StateFlow<List<BugReport>> = _bugReports.asStateFlow()

    // Feedback Items
    private val _feedbackItems = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbackItems: StateFlow<List<FeedbackItem>> = _feedbackItems.asStateFlow()

    // User/Content Reports
    private val _reports = MutableStateFlow<List<ReportItem>>(emptyList())
    val reports: StateFlow<List<ReportItem>> = _reports.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Account Recovery Requests
    private val _recoveryRequests = MutableStateFlow<List<AccountRecoveryRequest>>(emptyList())
    val recoveryRequests: StateFlow<List<AccountRecoveryRequest>> = _recoveryRequests.asStateFlow()

    // Group Invites & Join Requests
    private val _groupInvites = MutableStateFlow<List<GroupInvite>>(emptyList())
    val groupInvites: StateFlow<List<GroupInvite>> = _groupInvites.asStateFlow()

    private val _joinRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequest>> = _joinRequests.asStateFlow()

    // Active Sessions for Current User
    private val _activeSessions = MutableStateFlow<List<SessionInfo>>(emptyList())
    val activeSessions: StateFlow<List<SessionInfo>> = _activeSessions.asStateFlow()

    // Theme Customization
    private val _themeConfig = MutableStateFlow(ChatThemeCustomization())
    val themeConfig: StateFlow<ChatThemeCustomization> = _themeConfig.asStateFlow()

    // Offline Mode Simulation
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    init {
        loadData()
    }

    // ----------------------------------------------------
    // Password Security & Hashing
    // ----------------------------------------------------
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$password:$salt"
        val hash = md.digest(combined.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    // ----------------------------------------------------
    // Authentication & Account Recovery
    // ----------------------------------------------------
    fun register(email: String, username: String, displayName: String, pass: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim().lowercase().removePrefix("@")
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (cleanUsername.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val existing = _users.value.find { it.email.equals(cleanEmail, true) || it.username.equals(cleanUsername, true) }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Email or username is already registered."))
        }

        val salt = generateSalt()
        val hash = hashPassword(pass, salt)
        val newUser = User(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            email = cleanEmail,
            username = cleanUsername,
            displayName = displayName.ifBlank { cleanUsername.replaceFirstChar { it.uppercase() } },
            avatarUrl = "boy_avatar_1",
            bio = "Hey! Let's connect on Friends Chat.",
            statusMessage = "Active & coding ⚡",
            passwordSalt = salt,
            passwordHash = hash,
            role = if (_users.value.isEmpty()) UserRole.SUPER_ADMIN else UserRole.USER
        )

        val updated = _users.value + newUser
        _users.value = updated
        saveUsers(updated)
        loginUser(newUser)
        return Result.success(newUser)
    }

    fun login(emailOrUser: String, pass: String): Result<User> {
        val query = emailOrUser.trim().lowercase().removePrefix("@")
        val user = _users.value.find {
            it.email.equals(query, true) || it.username.equals(query, true)
        } ?: return Result.failure(IllegalArgumentException("No account found with this email or username."))

        if (user.isBanned) {
            return Result.failure(IllegalStateException("This account has been suspended by a moderator."))
        }

        val testHash = hashPassword(pass, user.passwordSalt)
        if (testHash != user.passwordHash) {
            return Result.failure(IllegalArgumentException("Incorrect password."))
        }

        loginUser(user)
        return Result.success(user)
    }

    private fun loginUser(user: User) {
        _currentUser.value = user
        prefs.edit().putString("logged_user_id", user.id).apply()

        // Create session
        val currentSession = SessionInfo(
            id = "session_${UUID.randomUUID().toString().take(6)}",
            deviceName = "Android Pixel Device (Current)",
            ipAddress = "192.168.1.104",
            location = "Local Device",
            isCurrentDevice = true,
            loginTimestamp = System.currentTimeMillis()
        )
        val backupSession = SessionInfo(
            id = "session_web_demo",
            deviceName = "Chrome Web Session",
            ipAddress = "104.28.19.42",
            location = "Singapore",
            isCurrentDevice = false,
            loginTimestamp = System.currentTimeMillis() - 86400000L
        )
        _activeSessions.value = listOf(currentSession, backupSession)
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().remove("logged_user_id").apply()
        _activeSessions.value = emptyList()
    }

    fun logoutAllSessions() {
        val curr = _currentUser.value
        logout()
        if (curr != null) {
            recordAudit("LOGOUT_ALL", "USER", curr.id, "User requested global session termination")
        }
    }

    fun deleteAccount(): Boolean {
        val user = _currentUser.value ?: return false
        val updated = _users.value.filter { it.id != user.id }
        _users.value = updated
        saveUsers(updated)
        recordAudit("DELETE_ACCOUNT", "USER", user.id, "User voluntarily deleted account")
        logout()
        return true
    }

    fun switchAccount(targetUserId: String) {
        val user = _users.value.find { it.id == targetUserId } ?: return
        loginUser(user)
    }

    fun submitAccountRecovery(email: String, username: String, reason: String): AccountRecoveryRequest {
        val req = AccountRecoveryRequest(
            id = "rec_${UUID.randomUUID().toString().take(6)}",
            email = email.trim(),
            username = username.trim(),
            reason = reason,
            recoveryCode = (100000..999999).random().toString()
        )
        val updated = listOf(req) + _recoveryRequests.value
        _recoveryRequests.value = updated
        recordAudit("SUBMIT_RECOVERY", "RECOVERY_REQUEST", req.id, "User submitted account recovery request")
        return req
    }

    fun resolveAccountRecovery(requestId: String, newPass: String, status: RecoveryStatus): Boolean {
        val req = _recoveryRequests.value.find { it.id == requestId } ?: return false
        val targetUser = _users.value.find { it.email.equals(req.email, true) || it.username.equals(req.username, true) }
        if (targetUser != null && newPass.isNotBlank()) {
            val salt = generateSalt()
            val hash = hashPassword(newPass, salt)
            val updatedUser = targetUser.copy(passwordSalt = salt, passwordHash = hash)
            val userList = _users.value.map { if (it.id == updatedUser.id) updatedUser else it }
            _users.value = userList
            saveUsers(userList)
        }
        val updatedReqs = _recoveryRequests.value.map {
            if (it.id == requestId) it.copy(status = status, moderatorNotes = "Handled by support") else it
        }
        _recoveryRequests.value = updatedReqs
        val mod = _currentUser.value
        recordAudit("RESOLVE_RECOVERY", "RECOVERY_REQUEST", requestId, "Recovery status changed to $status by ${mod?.displayName}")
        return true
    }

    // ----------------------------------------------------
    // User Profile & Privacy
    // ----------------------------------------------------
    fun updateProfile(displayName: String, bio: String, statusMessage: String, avatarUrl: String) {
        val curr = _currentUser.value ?: return
        val updated = curr.copy(
            displayName = displayName,
            bio = bio,
            statusMessage = statusMessage,
            avatarUrl = avatarUrl
        )
        _currentUser.value = updated
        val list = _users.value.map { if (it.id == updated.id) updated else it }
        _users.value = list
        saveUsers(list)
    }

    fun updatePrivacySettings(settings: PrivacySettings) {
        val curr = _currentUser.value ?: return
        val updated = curr.copy(privacySettings = settings)
        _currentUser.value = updated
        val list = _users.value.map { if (it.id == updated.id) updated else it }
        _users.value = list
        saveUsers(list)
    }

    // ----------------------------------------------------
    // Offline Simulation Toggle
    // ----------------------------------------------------
    fun setOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
        if (!offline) {
            // Process queue: deliver pending messages
            processOfflineQueue()
        }
    }

    private fun processOfflineQueue() {
        val currentMsgs = _messages.value.toMutableMap()
        var changed = false
        currentMsgs.forEach { (chatId, list) ->
            val updatedList = list.map { msg ->
                if (msg.status == MessageStatus.SENDING) {
                    changed = true
                    msg.copy(status = MessageStatus.DELIVERED)
                } else msg
            }
            currentMsgs[chatId] = updatedList
        }
        if (changed) {
            _messages.value = currentMsgs
        }
    }

    // ----------------------------------------------------
    // Messaging Engine (1-on-1 and Groups)
    // ----------------------------------------------------
    fun sendMessage(
        chatId: String,
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
    ): Message {
        val curr = _currentUser.value ?: _users.value.first()
        val initialStatus = if (_isOfflineMode.value) MessageStatus.SENDING else MessageStatus.SENT

        val pollOptionObjs = pollOptions.mapIndexed { idx, opt ->
            PollOption(id = "opt_$idx", text = opt, votes = emptyList())
        }

        val msg = Message(
            id = "msg_${UUID.randomUUID().toString().take(10)}",
            chatId = chatId,
            senderId = curr.id,
            senderName = curr.displayName,
            content = content,
            timestamp = System.currentTimeMillis(),
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
            pollOptions = pollOptionObjs,
            replyToMessageId = replyToMessageId,
            replyToContent = replyToContent,
            replyToSender = replyToSender,
            status = initialStatus
        )

        val chatMsgs = (_messages.value[chatId] ?: emptyList()) + msg
        val updatedMap = _messages.value.toMutableMap()
        updatedMap[chatId] = chatMsgs
        _messages.value = updatedMap

        // Update chat's last message and timestamp
        val chatList = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(lastMessage = msg, updatedAt = msg.timestamp)
            } else chat
        }
        _chats.value = chatList

        // Simulate delivery and read if online
        if (!_isOfflineMode.value) {
            coroutineScope.launch {
                delay(600)
                updateMessageStatus(chatId, msg.id, MessageStatus.DELIVERED)
                delay(1200)
                updateMessageStatus(chatId, msg.id, MessageStatus.READ)
            }
        }

        return msg
    }

    private fun updateMessageStatus(chatId: String, msgId: String, status: MessageStatus) {
        val list = _messages.value[chatId] ?: return
        val updated = list.map { if (it.id == msgId) it.copy(status = status) else it }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun addReaction(chatId: String, messageId: String, emoji: String) {
        val curr = _currentUser.value ?: return
        val list = _messages.value[chatId] ?: return
        val updated = list.map { msg ->
            if (msg.id == messageId) {
                val currentReactions = msg.reactions.toMutableMap()
                val userList = (currentReactions[emoji] ?: emptyList()).toMutableList()
                if (userList.contains(curr.id)) {
                    userList.remove(curr.id)
                    if (userList.isEmpty()) currentReactions.remove(emoji) else currentReactions[emoji] = userList
                } else {
                    userList.add(curr.id)
                    currentReactions[emoji] = userList
                }
                msg.copy(reactions = currentReactions)
            } else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun editMessage(chatId: String, messageId: String, newContent: String) {
        val list = _messages.value[chatId] ?: return
        val updated = list.map { msg ->
            if (msg.id == messageId) msg.copy(content = newContent, isEdited = true) else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun togglePinMessage(chatId: String, messageId: String) {
        val list = _messages.value[chatId] ?: return
        var targetPinned = false
        val updated = list.map { msg ->
            if (msg.id == messageId) {
                targetPinned = !msg.isPinned
                msg.copy(isPinned = targetPinned)
            } else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map

        val chatList = _chats.value.map {
            if (it.id == chatId) it.copy(pinnedMessageId = if (targetPinned) messageId else null) else it
        }
        _chats.value = chatList
    }

    fun toggleStarMessage(chatId: String, messageId: String) {
        val list = _messages.value[chatId] ?: return
        val updated = list.map { msg ->
            if (msg.id == messageId) msg.copy(isStarred = !msg.isStarred) else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun deleteMessageForMe(chatId: String, messageId: String) {
        val curr = _currentUser.value ?: return
        val list = _messages.value[chatId] ?: return
        val updated = list.map { msg ->
            if (msg.id == messageId) msg.copy(deletedForUserIds = msg.deletedForUserIds + curr.id) else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun deleteMessageForEveryone(chatId: String, messageId: String) {
        val list = _messages.value[chatId] ?: return
        val updated = list.map { msg ->
            if (msg.id == messageId) msg.copy(isDeletedForEveryone = true, content = "This message was deleted") else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun votePoll(chatId: String, messageId: String, optionId: String) {
        val curr = _currentUser.value ?: return
        val list = _messages.value[chatId] ?: return
        val updated = list.map { msg ->
            if (msg.id == messageId) {
                val newOptions = msg.pollOptions.map { opt ->
                    val voters = opt.votes.toMutableList()
                    if (opt.id == optionId) {
                        if (voters.contains(curr.id)) voters.remove(curr.id) else voters.add(curr.id)
                    } else {
                        // Single choice poll: remove vote from other options
                        voters.remove(curr.id)
                    }
                    opt.copy(votes = voters)
                }
                msg.copy(pollOptions = newOptions)
            } else msg
        }
        val map = _messages.value.toMutableMap()
        map[chatId] = updated
        _messages.value = map
    }

    fun forwardMessage(sourceChatId: String, messageId: String, targetChatId: String) {
        val msg = _messages.value[sourceChatId]?.find { it.id == messageId } ?: return
        sendMessage(
            chatId = targetChatId,
            content = msg.content,
            type = msg.type,
            mediaUrl = msg.mediaUrl,
            mediaSizeText = msg.mediaSizeText
        )
    }

    // ----------------------------------------------------
    // Groups & Communities Administration
    // ----------------------------------------------------
    fun createGroup(name: String, description: String, type: GroupType, memberUserIds: List<String>): Chat {
        val curr = _currentUser.value ?: _users.value.first()
        val groupId = "group_${UUID.randomUUID().toString().take(8)}"
        val creatorMember = GroupMember(
            userId = curr.id,
            displayName = curr.displayName,
            username = curr.username,
            avatarUrl = curr.avatarUrl,
            rank = AdminRank.CREATOR,
            permissions = GroupPermission.values().toSet()
        )

        val otherMembers = _users.value.filter { memberUserIds.contains(it.id) }.map {
            GroupMember(
                userId = it.id,
                displayName = it.displayName,
                username = it.username,
                avatarUrl = it.avatarUrl,
                rank = AdminRank.MEMBER,
                permissions = emptySet()
            )
        }

        val allMembers = listOf(creatorMember) + otherMembers
        val newChat = Chat(
            id = groupId,
            name = name,
            isGroup = true,
            groupType = type,
            creatorId = curr.id,
            description = description,
            avatarUrl = "group_badge_${(1..6).random()}",
            participantIds = allMembers.map { it.userId },
            members = allMembers,
            updatedAt = System.currentTimeMillis()
        )

        _chats.value = listOf(newChat) + _chats.value

        // Initial welcome announcement message
        sendMessage(
            chatId = groupId,
            content = "🎉 Welcome to $name! Group created by ${curr.displayName}.",
            type = MessageType.TEXT
        )

        recordAudit("CREATE_GROUP", "GROUP", groupId, "Created ${type.label}: $name")
        return newChat
    }

    fun updateGroupAdminRank(
        groupId: String,
        targetUserId: String,
        newRank: AdminRank,
        permissions: Set<GroupPermission>
    ): Boolean {
        val curr = _currentUser.value ?: return false
        val chat = _chats.value.find { it.id == groupId } ?: return false
        val callerMember = chat.members.find { it.userId == curr.id } ?: return false
        val targetMember = chat.members.find { it.userId == targetUserId } ?: return false

        // Hierarchy rule: Caller must outrank target to change permissions
        if (callerMember.rank.level <= targetMember.rank.level && callerMember.rank != AdminRank.CREATOR) {
            return false
        }
        if (callerMember.rank != AdminRank.CREATOR && !callerMember.permissions.contains(GroupPermission.MANAGE_ADMINS)) {
            return false
        }

        val updatedMembers = chat.members.map {
            if (it.userId == targetUserId) {
                it.copy(rank = newRank, permissions = permissions)
            } else it
        }

        _chats.value = _chats.value.map { if (it.id == groupId) it.copy(members = updatedMembers) else it }
        recordAudit("UPDATE_ADMIN_RANK", "GROUP_MEMBER", targetUserId, "Updated rank to ${newRank.title} in $groupId")
        return true
    }

    fun suspendAdminPrivileges(
        groupId: String,
        targetUserId: String,
        durationMinutes: Long,
        isPermanent: Boolean,
        reason: String
    ): Boolean {
        val curr = _currentUser.value ?: return false
        val chat = _chats.value.find { it.id == groupId } ?: return false
        val caller = chat.members.find { it.userId == curr.id } ?: return false
        val target = chat.members.find { it.userId == targetUserId } ?: return false

        if (caller.rank.level <= target.rank.level && caller.rank != AdminRank.CREATOR) {
            return false
        }

        val suspendUntil = if (isPermanent) Long.MAX_VALUE else System.currentTimeMillis() + (durationMinutes * 60000L)
        val updatedMembers = chat.members.map {
            if (it.userId == targetUserId) {
                it.copy(isSuspended = true, suspendedUntil = suspendUntil)
            } else it
        }

        _chats.value = _chats.value.map { if (it.id == groupId) it.copy(members = updatedMembers) else it }
        recordAudit("SUSPEND_ADMIN", "GROUP_MEMBER", targetUserId, "Suspended admin in $groupId for $durationMinutes min. Reason: $reason")
        return true
    }

    fun restoreAdminPrivileges(groupId: String, targetUserId: String): Boolean {
        val chat = _chats.value.find { it.id == groupId } ?: return false
        val updatedMembers = chat.members.map {
            if (it.userId == targetUserId) {
                it.copy(isSuspended = false, suspendedUntil = 0L)
            } else it
        }
        _chats.value = _chats.value.map { if (it.id == groupId) it.copy(members = updatedMembers) else it }
        recordAudit("RESTORE_ADMIN", "GROUP_MEMBER", targetUserId, "Restored admin privileges in $groupId")
        return true
    }

    fun removeGroupMember(groupId: String, targetUserId: String): Boolean {
        val curr = _currentUser.value ?: return false
        val chat = _chats.value.find { it.id == groupId } ?: return false
        val caller = chat.members.find { it.userId == curr.id } ?: return false
        val target = chat.members.find { it.userId == targetUserId } ?: return false

        if (caller.rank.level <= target.rank.level && caller.rank != AdminRank.CREATOR) {
            return false
        }

        val updatedMembers = chat.members.filter { it.userId != targetUserId }
        val updatedParticipants = chat.participantIds.filter { it != targetUserId }
        _chats.value = _chats.value.map {
            if (it.id == groupId) it.copy(members = updatedMembers, participantIds = updatedParticipants) else it
        }
        recordAudit("REMOVE_MEMBER", "GROUP_MEMBER", targetUserId, "Removed member from $groupId")
        return true
    }

    fun createInviteLink(groupId: String, maxUses: Int = 100, expiresHours: Int = 168): GroupInvite {
        val curr = _currentUser.value ?: _users.value.first()
        val invite = GroupInvite(
            id = "inv_${UUID.randomUUID().toString().take(6)}",
            groupId = groupId,
            code = "FC-${UUID.randomUUID().toString().take(6).uppercase()}",
            creatorId = curr.id,
            expiresAt = System.currentTimeMillis() + (expiresHours * 3600000L),
            maxUses = maxUses,
            usedCount = 0
        )
        _groupInvites.value = listOf(invite) + _groupInvites.value
        return invite
    }

    fun revokeInviteLink(inviteId: String) {
        _groupInvites.value = _groupInvites.value.map {
            if (it.id == inviteId) it.copy(isRevoked = true) else it
        }
    }

    fun submitJoinRequest(groupId: String) {
        val curr = _currentUser.value ?: return
        val req = JoinRequest(
            id = "req_${UUID.randomUUID().toString().take(6)}",
            groupId = groupId,
            userId = curr.id,
            userName = curr.displayName,
            userAvatar = curr.avatarUrl
        )
        _joinRequests.value = listOf(req) + _joinRequests.value
    }

    fun handleJoinRequest(requestId: String, approve: Boolean) {
        val req = _joinRequests.value.find { it.id == requestId } ?: return
        val newStatus = if (approve) JoinRequestStatus.APPROVED else JoinRequestStatus.REJECTED
        _joinRequests.value = _joinRequests.value.map {
            if (it.id == requestId) it.copy(status = newStatus) else it
        }

        if (approve) {
            val user = _users.value.find { it.id == req.userId }
            if (user != null) {
                val chat = _chats.value.find { it.id == req.groupId }
                if (chat != null && !chat.participantIds.contains(user.id)) {
                    val newMember = GroupMember(
                        userId = user.id,
                        displayName = user.displayName,
                        username = user.username,
                        avatarUrl = user.avatarUrl
                    )
                    val updated = chat.copy(
                        members = chat.members + newMember,
                        participantIds = chat.participantIds + user.id
                    )
                    _chats.value = _chats.value.map { if (it.id == req.groupId) updated else it }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Status / Updates Engine
    // ----------------------------------------------------
    fun postStatus(
        textContent: String = "",
        mediaUrl: String = "",
        contentType: StatusContentType = StatusContentType.TEXT,
        caption: String = "",
        backgroundHex: String = "#0369A1",
        privacyMode: VisibilityOption = VisibilityOption.MY_CONTACTS,
        isPermanent: Boolean = false
    ): StatusItem {
        val curr = _currentUser.value ?: _users.value.first()
        val item = StatusItem(
            id = "status_${UUID.randomUUID().toString().take(8)}",
            userId = curr.id,
            userName = curr.displayName,
            userAvatar = curr.avatarUrl,
            contentType = contentType,
            textContent = textContent,
            mediaUrl = mediaUrl,
            caption = caption,
            backgroundHex = backgroundHex,
            timestamp = System.currentTimeMillis(),
            expiresAt = if (isPermanent) Long.MAX_VALUE else System.currentTimeMillis() + 86400000L,
            isPermanentUpdate = isPermanent,
            privacyMode = privacyMode
        )
        _statusItems.value = listOf(item) + _statusItems.value
        return item
    }

    fun recordStatusView(statusId: String) {
        val curr = _currentUser.value ?: return
        val list = _statusItems.value
        val updated = list.map { item ->
            if (item.id == statusId && item.userId != curr.id && item.viewers.none { it.userId == curr.id }) {
                val newViewer = StatusViewer(curr.id, curr.displayName, curr.avatarUrl, System.currentTimeMillis())
                item.copy(viewers = item.viewers + newViewer)
            } else item
        }
        _statusItems.value = updated
    }

    // ----------------------------------------------------
    // Calls
    // ----------------------------------------------------
    fun recordCall(remoteUserId: String, remoteUserName: String, remoteAvatar: String, type: CallType, isIncoming: Boolean, status: CallStatus, durationSec: Int) {
        val record = CallRecord(
            id = "call_${UUID.randomUUID().toString().take(6)}",
            remoteUserId = remoteUserId,
            remoteUserName = remoteUserName,
            remoteUserAvatar = remoteAvatar,
            callType = type,
            isIncoming = isIncoming,
            status = status,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSec
        )
        _callRecords.value = listOf(record) + _callRecords.value
    }

    // ----------------------------------------------------
    // Moderation, Bugs, Feedback & Reports
    // ----------------------------------------------------
    fun submitBugReport(category: String, title: String, steps: String, severity: BugSeverity, screenshotUri: String? = null): BugReport {
        val curr = _currentUser.value ?: _users.value.first()
        val report = BugReport(
            id = "BUG-${(1000..9999).random()}",
            userId = curr.id,
            userEmail = curr.email,
            category = category,
            severity = severity,
            title = title,
            stepsToReproduce = steps,
            screenshotUri = screenshotUri
        )
        _bugReports.value = listOf(report) + _bugReports.value
        recordAudit("SUBMIT_BUG", "BUG_REPORT", report.id, "User submitted bug: $title")
        return report
    }

    fun updateBugStatus(reportId: String, newStatus: BugStatus, notes: String) {
        val curr = _currentUser.value
        _bugReports.value = _bugReports.value.map {
            if (it.id == reportId) it.copy(status = newStatus, moderatorNotes = notes) else it
        }
        recordAudit("UPDATE_BUG_STATUS", "BUG_REPORT", reportId, "Status -> $newStatus by ${curr?.displayName}")
    }

    fun submitFeedback(rating: Int, category: FeedbackCategory, comments: String): FeedbackItem {
        val curr = _currentUser.value ?: _users.value.first()
        val fb = FeedbackItem(
            id = "FB-${(1000..9999).random()}",
            userId = curr.id,
            userEmail = curr.email,
            rating = rating,
            category = category,
            comments = comments
        )
        _feedbackItems.value = listOf(fb) + _feedbackItems.value
        return fb
    }

    fun updateFeedbackStatus(fbId: String, newStatus: FeedbackStatus, response: String) {
        val curr = _currentUser.value
        _feedbackItems.value = _feedbackItems.value.map {
            if (it.id == fbId) it.copy(status = newStatus, adminResponse = response) else it
        }
        recordAudit("UPDATE_FEEDBACK_STATUS", "FEEDBACK", fbId, "Status -> $newStatus by ${curr?.displayName}")
    }

    fun submitReport(
        type: ReportTargetType,
        targetId: String,
        targetTitle: String,
        category: ReportCategory,
        description: String
    ): ReportItem {
        val curr = _currentUser.value ?: _users.value.first()
        val rep = ReportItem(
            id = "REP-${(1000..9999).random()}",
            reporterId = curr.id,
            reportedType = type,
            reportedId = targetId,
            reportedTitle = targetTitle,
            category = category,
            description = description
        )
        _reports.value = listOf(rep) + _reports.value
        recordAudit("SUBMIT_REPORT", type.name, targetId, "Reported for ${category.label}")
        return rep
    }

    fun handleReportAction(reportId: String, action: ReportStatus, banTarget: Boolean = false, notes: String = "") {
        val curr = _currentUser.value
        val rep = _reports.value.find { it.id == reportId } ?: return
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(status = action, resolutionNotes = notes) else it
        }

        if (banTarget && rep.reportedType == ReportTargetType.USER) {
            _users.value = _users.value.map {
                if (it.id == rep.reportedId) it.copy(isBanned = true) else it
            }
        }
        recordAudit("RESOLVE_REPORT", "REPORT", reportId, "Report marked as $action by ${curr?.displayName}. Ban=$banTarget")
    }

    fun recordAudit(action: String, targetType: String, targetId: String, reason: String) {
        val curr = _currentUser.value
        val log = AuditLog(
            id = "audit_${UUID.randomUUID().toString().take(6)}",
            moderatorId = curr?.id ?: "system",
            moderatorName = curr?.displayName ?: "System Automator",
            action = action,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // ----------------------------------------------------
    // Theme & Wallpaper Customization Engine
    // ----------------------------------------------------
    fun updateTheme(custom: ChatThemeCustomization) {
        _themeConfig.value = custom
        prefs.edit()
            .putString("theme_palette", custom.selectedPalette.name)
            .putBoolean("theme_dark", custom.isDarkMode)
            .putString("theme_bubble", custom.bubbleShape.name)
            .putString("theme_wallpaper", custom.wallpaperType.name)
            .putString("theme_wallpaper_uri", custom.customWallpaperUri ?: "")
            .putFloat("theme_brightness", custom.wallpaperBrightness)
            .putFloat("theme_opacity", custom.wallpaperOpacity)
            .putFloat("theme_blur", custom.wallpaperBlur)
            .apply()
    }

    fun exportThemeJson(): String {
        val t = _themeConfig.value
        val json = JSONObject()
        json.put("palette", t.selectedPalette.name)
        json.put("isDark", t.isDarkMode)
        json.put("bubble", t.bubbleShape.name)
        json.put("wallpaper", t.wallpaperType.name)
        json.put("brightness", t.wallpaperBrightness.toDouble())
        json.put("opacity", t.wallpaperOpacity.toDouble())
        return json.toString(2)
    }

    fun importThemeJson(jsonStr: String): Boolean {
        return try {
            val json = JSONObject(jsonStr)
            val palette = PaletteTheme.valueOf(json.optString("palette", PaletteTheme.CYBER_CYAN.name))
            val isDark = json.optBoolean("isDark", true)
            val bubble = BubbleShapeType.valueOf(json.optString("bubble", BubbleShapeType.ROUNDED_SLOPE.name))
            val wp = WallpaperType.valueOf(json.optString("wallpaper", WallpaperType.SUBTLE_GRID.name))
            val brightness = json.optDouble("brightness", 0.85).toFloat()
            val opacity = json.optDouble("opacity", 0.9).toFloat()

            val imported = ChatThemeCustomization(
                selectedPalette = palette,
                isDarkMode = isDark,
                bubbleShape = bubble,
                wallpaperType = wp,
                wallpaperBrightness = brightness,
                wallpaperOpacity = opacity
            )
            updateTheme(imported)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ----------------------------------------------------
    // Seed Realistic 50-User Network Data
    // ----------------------------------------------------
    private fun loadData() {
        val savedUsersJson = prefs.getString("users_json", null)
        val loadedUsers = if (!savedUsersJson.isNullOrEmpty()) {
            parseUsers(savedUsersJson)
        } else {
            generateDemoUsers()
        }
        _users.value = loadedUsers
        saveUsers(loadedUsers)

        // Find or set current user
        val loggedId = prefs.getString("logged_user_id", null)
        val sessionUser = loadedUsers.find { it.id == loggedId } ?: loadedUsers.first()
        loginUser(sessionUser)

        // Seed demo chats & groups
        seedDemoChatsAndMessages(loadedUsers, sessionUser)

        // Seed demo status stories
        seedDemoStatuses(loadedUsers)

        // Seed demo calls
        seedDemoCalls(loadedUsers)

        // Seed initial moderation tickets
        seedDemoModeration()

        // Load theme config
        val paletteName = prefs.getString("theme_palette", PaletteTheme.CYBER_CYAN.name) ?: PaletteTheme.CYBER_CYAN.name
        val isDark = prefs.getBoolean("theme_dark", true)
        val bubbleName = prefs.getString("theme_bubble", BubbleShapeType.ROUNDED_SLOPE.name) ?: BubbleShapeType.ROUNDED_SLOPE.name
        val wpName = prefs.getString("theme_wallpaper", WallpaperType.SUBTLE_GRID.name) ?: WallpaperType.SUBTLE_GRID.name
        val customUri = prefs.getString("theme_wallpaper_uri", null)
        val bright = prefs.getFloat("theme_brightness", 0.85f)
        val opac = prefs.getFloat("theme_opacity", 0.9f)
        val blur = prefs.getFloat("theme_blur", 0f)

        _themeConfig.value = ChatThemeCustomization(
            selectedPalette = runCatching { PaletteTheme.valueOf(paletteName) }.getOrDefault(PaletteTheme.CYBER_CYAN),
            isDarkMode = isDark,
            bubbleShape = runCatching { BubbleShapeType.valueOf(bubbleName) }.getOrDefault(BubbleShapeType.ROUNDED_SLOPE),
            wallpaperType = runCatching { WallpaperType.valueOf(wpName) }.getOrDefault(WallpaperType.SUBTLE_GRID),
            customWallpaperUri = customUri,
            wallpaperBrightness = bright,
            wallpaperOpacity = opac,
            wallpaperBlur = blur
        )
    }

    private fun generateDemoUsers(): List<User> {
        val names = listOf(
            "Alex Mercer", "Liam Chen", "Ryan Reynolds", "Ethan Walker", "Marcus Vance",
            "David Miller", "Lucas Kim", "Noah Garcia", "Tyler Brooks", "Jack Wilson",
            "Samir Khan", "Oliver Scott", "Leo Bennett", "Zack Taylor", "Daniel Clark",
            "Mason Cooper", "Chris Evans", "Jake Sullivan", "Nathan Drake", "Kai Tanaka",
            "Aaron Hayes", "Benjamin Moore", "Caleb Ross", "Dominic Stone", "Eli Sanchez",
            "Felix Meyer", "Gavin Reed", "Hunter Hall", "Ian Wright", "Jordan Bell",
            "Kieran Hughes", "Luke Patterson", "Maximus Troy", "Nico Adams", "Oscar Butler",
            "Parker Coleman", "Quinn Evans", "Reece Flores", "Sean Murphy", "Tristan Ward",
            "Victor Diaz", "Wesley Powell", "Xavier Wood", "Yusuf Ali", "Zachary Price",
            "Austin Gray", "Blake Howard", "Chase Watson", "Dean Winchester", "Dylan O'Connor"
        )

        return names.mapIndexed { i, name ->
            val uId = "usr_${i + 1}"
            val uName = name.lowercase().replace(" ", "_").replace("'", "")
            val salt = generateSalt()
            val hash = hashPassword("password123", salt)
            val role = when (i) {
                0 -> UserRole.SUPER_ADMIN
                1 -> UserRole.SENIOR_MODERATOR
                2 -> UserRole.MODERATOR
                else -> UserRole.USER
            }
            User(
                id = uId,
                email = "$uName@friendschat.app",
                username = uName,
                displayName = name,
                avatarUrl = "avatar_${(i % 12) + 1}",
                bio = when (i % 5) {
                    0 -> "Gaming, tech & late night conversations 🎮"
                    1 -> "Software dev & Android hacker 💻"
                    2 -> "Gym, music, espresso ☕💪"
                    3 -> "Building the future of social networks 🚀"
                    else -> "Offline is the new luxury ✨"
                },
                statusMessage = if (i % 2 == 0) "Online & ready" else "Busy at work",
                isOnline = i % 3 != 0,
                lastSeenTimestamp = System.currentTimeMillis() - (i * 360000L),
                passwordSalt = salt,
                passwordHash = hash,
                role = role
            )
        }
    }

    private fun seedDemoChatsAndMessages(userList: List<User>, me: User) {
        val chatsCreated = mutableListOf<Chat>()
        val messagesMap = mutableMapOf<String, List<Message>>()

        // Group 1: The Boyz Gaming Squad
        val g1Id = "chat_group_gaming"
        val g1Members = userList.take(8).mapIndexed { idx, u ->
            GroupMember(
                userId = u.id,
                displayName = u.displayName,
                username = u.username,
                avatarUrl = u.avatarUrl,
                rank = if (idx == 0) AdminRank.CREATOR else if (idx == 1) AdminRank.SENIOR_ADMIN else AdminRank.MEMBER,
                permissions = if (idx <= 1) GroupPermission.values().toSet() else emptySet()
            )
        }
        val g1 = Chat(
            id = g1Id,
            name = "The Boyz Gaming Squad 🎮",
            isGroup = true,
            groupType = GroupType.COMMUNITY,
            creatorId = g1Members.first().userId,
            avatarUrl = "group_badge_1",
            description = "Valorant, Warzone, and late night Discord banter.",
            participantIds = g1Members.map { it.userId },
            members = g1Members,
            unreadCount = 2,
            updatedAt = System.currentTimeMillis() - 120000L
        )
        chatsCreated.add(g1)

        val g1Msgs = listOf(
            Message(
                id = "m1_1",
                chatId = g1Id,
                senderId = userList[1].id,
                senderName = userList[1].displayName,
                content = "Yo squad, who is hopping on the server tonight?",
                timestamp = System.currentTimeMillis() - 7200000L,
                status = MessageStatus.READ
            ),
            Message(
                id = "m1_2",
                chatId = g1Id,
                senderId = userList[2].id,
                senderName = userList[2].displayName,
                content = "I'm in! Let's run competitive mode around 9 PM.",
                timestamp = System.currentTimeMillis() - 3600000L,
                status = MessageStatus.READ,
                reactions = mapOf("🔥" to listOf(userList[1].id, me.id))
            ),
            Message(
                id = "m1_3",
                chatId = g1Id,
                senderId = userList[3].id,
                senderName = userList[3].displayName,
                content = "Vote on what game we start with:",
                type = MessageType.POLL,
                pollQuestion = "Which game for Friday night lobby?",
                pollOptions = listOf(
                    PollOption("opt_1", "Valorant Ranked", listOf(userList[1].id, userList[2].id)),
                    PollOption("opt_2", "Call of Duty: Warzone", listOf(userList[3].id, me.id)),
                    PollOption("opt_3", "Rocket League", emptyList())
                ),
                timestamp = System.currentTimeMillis() - 1800000L,
                status = MessageStatus.READ
            ),
            Message(
                id = "m1_4",
                chatId = g1Id,
                senderId = userList[0].id,
                senderName = userList[0].displayName,
                content = "Voice channel is open, join in boys!",
                timestamp = System.currentTimeMillis() - 120000L,
                status = MessageStatus.READ
            )
        )
        messagesMap[g1Id] = g1Msgs

        // Group 2: Android Core Developers
        val g2Id = "chat_group_devs"
        val g2Members = userList.drop(4).take(10).mapIndexed { idx, u ->
            GroupMember(
                userId = u.id,
                displayName = u.displayName,
                username = u.username,
                avatarUrl = u.avatarUrl,
                rank = if (idx == 0) AdminRank.CREATOR else AdminRank.MEMBER,
                permissions = if (idx == 0) GroupPermission.values().toSet() else emptySet()
            )
        }
        val g2 = Chat(
            id = g2Id,
            name = "Android Core Engineers ⚡",
            isGroup = true,
            groupType = GroupType.PUBLIC,
            creatorId = g2Members.first().userId,
            avatarUrl = "group_badge_2",
            description = "Kotlin 2.2, Jetpack Compose, high-performance architecture.",
            participantIds = g2Members.map { it.userId },
            members = g2Members,
            updatedAt = System.currentTimeMillis() - 600000L
        )
        chatsCreated.add(g2)

        val g2Msgs = listOf(
            Message(
                id = "m2_1",
                chatId = g2Id,
                senderId = userList[4].id,
                senderName = userList[4].displayName,
                content = "Release .AAB bundle compiled with zero errors. Build verified for Play Store!",
                timestamp = System.currentTimeMillis() - 1800000L,
                type = MessageType.TEXT,
                status = MessageStatus.READ,
                reactions = mapOf("🚀" to listOf(userList[5].id, me.id))
            )
        )
        messagesMap[g2Id] = g2Msgs

        // Direct 1-on-1 chats with first 6 users
        val directFriends = userList.filter { it.id != me.id }.take(6)
        directFriends.forEachIndexed { idx, friend ->
            val chatId = "dm_${friend.id}"
            val lastContent = when (idx) {
                0 -> "Did you check out the new dark mode theme? Looks clean! 🔥"
                1 -> "Let's grab lunch tomorrow, ping me when free."
                2 -> "Sent you the project documentation file."
                3 -> "Audio note attached (0:24) 🎙️"
                4 -> "Check out this photo from the weekend hike!"
                else -> "Hey bro, are you around?"
            }
            val chat = Chat(
                id = chatId,
                name = friend.displayName,
                isGroup = false,
                avatarUrl = friend.avatarUrl,
                participantIds = listOf(me.id, friend.id),
                unreadCount = if (idx == 0) 1 else 0,
                updatedAt = System.currentTimeMillis() - (idx * 900000L)
            )
            chatsCreated.add(chat)

            val msgs = listOf(
                Message(
                    id = "msg_dm_${idx}_1",
                    chatId = chatId,
                    senderId = friend.id,
                    senderName = friend.displayName,
                    content = lastContent,
                    timestamp = System.currentTimeMillis() - (idx * 900000L),
                    type = when (idx) {
                        2 -> MessageType.DOCUMENT
                        3 -> MessageType.AUDIO
                        4 -> MessageType.IMAGE
                        else -> MessageType.TEXT
                    },
                    mediaUrl = if (idx == 4) "sample_photo" else "",
                    mediaSizeText = if (idx == 2) "2.4 MB (PDF)" else "",
                    mediaDurationSec = if (idx == 3) 24 else 0,
                    status = MessageStatus.READ
                )
            )
            messagesMap[chatId] = msgs
        }

        _chats.value = chatsCreated
        _messages.value = messagesMap
    }

    private fun seedDemoStatuses(userList: List<User>) {
        val statuses = listOf(
            StatusItem(
                id = "st_1",
                userId = userList[1].id,
                userName = userList[1].displayName,
                userAvatar = userList[1].avatarUrl,
                contentType = StatusContentType.TEXT,
                textContent = "Cruising late night with the synthwave playlist 🌃🎧",
                backgroundHex = "#0284C7",
                timestamp = System.currentTimeMillis() - 7200000L
            ),
            StatusItem(
                id = "st_2",
                userId = userList[2].id,
                userName = userList[2].displayName,
                userAvatar = userList[2].avatarUrl,
                contentType = StatusContentType.IMAGE,
                caption = "New mechanical keyboard setup! Clicky blue switches ⌨️",
                mediaUrl = "setup_art",
                backgroundHex = "#0F172A",
                timestamp = System.currentTimeMillis() - 14400000L
            ),
            StatusItem(
                id = "st_3",
                userId = userList[3].id,
                userName = userList[3].displayName,
                userAvatar = userList[3].avatarUrl,
                contentType = StatusContentType.TEXT,
                textContent = "Gym session crushed 💪 Heavy deadlifts today!",
                backgroundHex = "#10B981",
                timestamp = System.currentTimeMillis() - 21600000L
            )
        )
        _statusItems.value = statuses
    }

    private fun seedDemoCalls(userList: List<User>) {
        val calls = listOf(
            CallRecord(
                id = "c_1",
                remoteUserId = userList[1].id,
                remoteUserName = userList[1].displayName,
                remoteUserAvatar = userList[1].avatarUrl,
                callType = CallType.VOICE,
                isIncoming = true,
                status = CallStatus.ANSWERED,
                timestamp = System.currentTimeMillis() - 18000000L,
                durationSeconds = 342
            ),
            CallRecord(
                id = "c_2",
                remoteUserId = userList[2].id,
                remoteUserName = userList[2].displayName,
                remoteUserAvatar = userList[2].avatarUrl,
                callType = CallType.VIDEO,
                isIncoming = true,
                status = CallStatus.MISSED,
                timestamp = System.currentTimeMillis() - 36000000L
            ),
            CallRecord(
                id = "c_3",
                remoteUserId = userList[3].id,
                remoteUserName = userList[3].displayName,
                remoteUserAvatar = userList[3].avatarUrl,
                callType = CallType.VOICE,
                isIncoming = false,
                status = CallStatus.ANSWERED,
                timestamp = System.currentTimeMillis() - 86400000L,
                durationSeconds = 120
            )
        )
        _callRecords.value = calls
    }

    private fun seedDemoModeration() {
        val bugs = listOf(
            BugReport(
                id = "BUG-1042",
                userId = "usr_5",
                userEmail = "david_miller@friendschat.app",
                category = "UI / Display",
                severity = BugSeverity.MEDIUM,
                title = "Wallpaper brightness slider resets on app resume",
                stepsToReproduce = "1. Open Theme Customizer\n2. Set brightness to 50%\n3. Background app and reopen",
                status = BugStatus.IN_PROGRESS
            ),
            BugReport(
                id = "BUG-1043",
                userId = "usr_8",
                userEmail = "noah_garcia@friendschat.app",
                category = "Notifications",
                severity = BugSeverity.LOW,
                title = "Muted group still shows in-app toast preview",
                stepsToReproduce = "1. Mute gaming group for 8 hours\n2. Receive message",
                status = BugStatus.SUBMITTED
            )
        )
        _bugReports.value = bugs

        val feedback = listOf(
            FeedbackItem(
                id = "FB-2001",
                userId = "usr_3",
                userEmail = "ryan_reynolds@friendschat.app",
                rating = 5,
                category = FeedbackCategory.FEATURE_REQUEST,
                comments = "Love the boyish slate cyan aesthetic! Please add custom sound effects for sent messages.",
                status = FeedbackStatus.REVIEWED
            )
        )
        _feedbackItems.value = feedback

        val recovery = listOf(
            AccountRecoveryRequest(
                id = "REC-301",
                email = "lost_account@friendschat.app",
                username = "lost_gamer",
                reason = "Changed phone number and forgot password",
                recoveryCode = "749214",
                status = RecoveryStatus.PENDING
            )
        )
        _recoveryRequests.value = recovery

        val audits = listOf(
            AuditLog(
                id = "aud_init",
                moderatorId = "usr_1",
                moderatorName = "Alex Mercer (Super Admin)",
                action = "SYSTEM_INITIALIZE",
                targetType = "SECURITY",
                targetId = "SYS-1",
                reason = "Friends Chat system deployed and verified for Google Play AAB release."
            )
        )
        _auditLogs.value = audits
    }

    private fun saveUsers(users: List<User>) {
        val array = JSONArray()
        users.forEach { u ->
            val obj = JSONObject()
            obj.put("id", u.id)
            obj.put("email", u.email)
            obj.put("username", u.username)
            obj.put("displayName", u.displayName)
            obj.put("avatarUrl", u.avatarUrl)
            obj.put("bio", u.bio)
            obj.put("statusMessage", u.statusMessage)
            obj.put("isOnline", u.isOnline)
            obj.put("lastSeenTimestamp", u.lastSeenTimestamp)
            obj.put("passwordSalt", u.passwordSalt)
            obj.put("passwordHash", u.passwordHash)
            obj.put("role", u.role.name)
            obj.put("isBanned", u.isBanned)
            array.put(obj)
        }
        prefs.edit().putString("users_json", array.toString()).apply()
    }

    private fun parseUsers(jsonStr: String): List<User> {
        val list = mutableListOf<User>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    User(
                        id = obj.getString("id"),
                        email = obj.getString("email"),
                        username = obj.getString("username"),
                        displayName = obj.getString("displayName"),
                        avatarUrl = obj.optString("avatarUrl", "avatar_1"),
                        bio = obj.optString("bio", ""),
                        statusMessage = obj.optString("statusMessage", ""),
                        isOnline = obj.optBoolean("isOnline", true),
                        lastSeenTimestamp = obj.optLong("lastSeenTimestamp", System.currentTimeMillis()),
                        passwordSalt = obj.optString("passwordSalt", ""),
                        passwordHash = obj.optString("passwordHash", ""),
                        role = runCatching { UserRole.valueOf(obj.getString("role")) }.getOrDefault(UserRole.USER),
                        isBanned = obj.optBoolean("isBanned", false)
                    )
                )
            }
        } catch (e: Exception) {
            return generateDemoUsers()
        }
        return list
    }
}
