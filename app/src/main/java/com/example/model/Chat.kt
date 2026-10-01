package com.example.model

data class Chat(
    val id: String,
    val name: String,
    val isGroup: Boolean = false,
    val groupType: GroupType = GroupType.PRIVATE,
    val creatorId: String? = null,
    val avatarUrl: String = "",
    val description: String = "",
    val participantIds: List<String> = emptyList(),
    val members: List<GroupMember> = emptyList(),
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val muteUntil: Long = 0L,
    val disappearingDurationSeconds: Int = 0,
    val customWallpaper: String? = null,
    val pinnedMessageId: String? = null,
    val showHistoryToNewMembers: Boolean = true,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
