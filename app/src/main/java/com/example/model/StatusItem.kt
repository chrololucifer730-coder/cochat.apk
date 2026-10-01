package com.example.model

enum class StatusContentType {
    TEXT, IMAGE, VIDEO
}

data class StatusViewer(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val viewedAt: Long = System.currentTimeMillis()
)

data class StatusItem(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val contentType: StatusContentType = StatusContentType.TEXT,
    val textContent: String = "",
    val mediaUrl: String = "",
    val caption: String = "",
    val backgroundHex: String = "#0369A1",
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L, // 24 hours
    val isPermanentUpdate: Boolean = false,
    val privacyMode: VisibilityOption = VisibilityOption.MY_CONTACTS,
    val viewers: List<StatusViewer> = emptyList(),
    val repliesCount: Int = 0
)
