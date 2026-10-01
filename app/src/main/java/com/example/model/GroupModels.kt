package com.example.model

enum class GroupType(val label: String) {
    PRIVATE("Private Group"),
    PUBLIC("Public Group"),
    COMMUNITY("Community"),
    ANNOUNCEMENT("Announcement Channel")
}

enum class AdminRank(val title: String, val level: Int) {
    CREATOR("Creator & Owner", 100),
    SENIOR_ADMIN("Senior Admin", 50),
    JUNIOR_ADMIN("Moderator Admin", 20),
    MEMBER("Member", 0)
}

enum class GroupPermission(val label: String) {
    ADD_MEMBERS("Add Members"),
    REMOVE_MEMBERS("Remove Members"),
    BAN_MEMBERS("Ban Members"),
    APPROVE_REQUESTS("Approve Join Requests"),
    CREATE_INVITES("Create Invite Links"),
    EDIT_GROUP_INFO("Edit Group Info"),
    DELETE_MESSAGES("Delete Messages"),
    PIN_MESSAGES("Pin Messages"),
    SEND_ANNOUNCEMENTS("Send Announcements"),
    MANAGE_ADMINS("Manage Admin Ranks")
}

data class GroupMember(
    val userId: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String = "",
    val rank: AdminRank = AdminRank.MEMBER,
    val permissions: Set<GroupPermission> = emptySet(),
    val isSuspended: Boolean = false,
    val suspendedUntil: Long = 0L,
    val joinedAt: Long = System.currentTimeMillis()
)

data class GroupInvite(
    val id: String,
    val groupId: String,
    val code: String,
    val creatorId: String,
    val expiresAt: Long = System.currentTimeMillis() + 86400000L * 7,
    val maxUses: Int = 50,
    val usedCount: Int = 0,
    val isRevoked: Boolean = false
)

enum class JoinRequestStatus { PENDING, APPROVED, REJECTED }

data class JoinRequest(
    val id: String,
    val groupId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val requestedAt: Long = System.currentTimeMillis(),
    val status: JoinRequestStatus = JoinRequestStatus.PENDING
)
