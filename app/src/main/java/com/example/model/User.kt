package com.example.model

enum class UserRole(val displayName: String, val level: Int) {
    USER("Member", 1),
    MODERATOR("Moderator", 2),
    SENIOR_MODERATOR("Senior Moderator", 3),
    SUPER_ADMIN("Super Admin", 4)
}

enum class VisibilityOption(val label: String) {
    EVERYONE("Everyone"),
    MY_CONTACTS("My Contacts"),
    NOBODY("Nobody")
}

data class PrivacySettings(
    val lastSeenVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val onlineStatusVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val profilePhotoVisibility: VisibilityOption = VisibilityOption.EVERYONE,
    val statusVisibility: VisibilityOption = VisibilityOption.MY_CONTACTS,
    val whoCanContactMe: VisibilityOption = VisibilityOption.EVERYONE,
    val whoCanAddToGroups: VisibilityOption = VisibilityOption.MY_CONTACTS,
    val readReceipts: Boolean = true
)

data class User(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val bio: String = "Hey there! I am using Friends Chat.",
    val statusMessage: String = "Online & available 🚀",
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val passwordSalt: String = "",
    val passwordHash: String = "",
    val role: UserRole = UserRole.USER,
    val isBanned: Boolean = false,
    val privacySettings: PrivacySettings = PrivacySettings(),
    val createdAt: Long = System.currentTimeMillis()
)
