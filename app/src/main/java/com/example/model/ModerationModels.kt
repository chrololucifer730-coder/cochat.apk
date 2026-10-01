package com.example.model

enum class BugSeverity(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

enum class BugStatus(val label: String) {
    SUBMITTED("Submitted"),
    REVIEWING("In Review"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved")
}

data class BugReport(
    val id: String,
    val userId: String,
    val userEmail: String,
    val category: String,
    val severity: BugSeverity = BugSeverity.MEDIUM,
    val title: String,
    val stepsToReproduce: String,
    val screenshotUri: String? = null,
    val deviceModel: String = "Pixel 8 Pro",
    val androidVersion: String = "Android 14 (API 34)",
    val appVersion: String = "1.0.0 (Bundle AAB)",
    val status: BugStatus = BugStatus.SUBMITTED,
    val moderatorNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class FeedbackCategory(val label: String) {
    FEATURE_REQUEST("Feature Request"),
    UI_UX("UI / Design Feedback"),
    PERFORMANCE("Performance"),
    SUGGESTION("Product Suggestion"),
    COMPLAINT("Complaint"),
    OTHER("Other")
}

enum class FeedbackStatus(val label: String) {
    NEW("New"),
    REVIEWED("Reviewed"),
    PLANNED("Planned"),
    IMPLEMENTED("Implemented"),
    ARCHIVED("Archived")
}

data class FeedbackItem(
    val id: String,
    val userId: String,
    val userEmail: String,
    val rating: Int = 5,
    val category: FeedbackCategory = FeedbackCategory.FEATURE_REQUEST,
    val comments: String,
    val status: FeedbackStatus = FeedbackStatus.NEW,
    val adminResponse: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class ReportTargetType { USER, MESSAGE, GROUP, STATUS }
enum class ReportCategory(val label: String) {
    SPAM("Spam / Bots"),
    HARASSMENT("Harassment / Bullying"),
    INAPPROPRIATE("Inappropriate Content"),
    IMPERSONATION("Impersonation"),
    ABUSE("Abuse / Hate Speech"),
    PRIVACY_SECURITY("Privacy / Security Concern"),
    OTHER("Other")
}

enum class ReportStatus(val label: String) {
    SUBMITTED("Submitted"),
    REVIEWING("Under Review"),
    ACTION_TAKEN("Action Taken"),
    DISMISSED("Dismissed"),
    CLOSED("Closed")
}

data class ReportItem(
    val id: String,
    val reporterId: String,
    val reportedType: ReportTargetType,
    val reportedId: String,
    val reportedTitle: String,
    val category: ReportCategory,
    val description: String,
    val screenshotUri: String? = null,
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val resolutionNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AuditLog(
    val id: String,
    val moderatorId: String,
    val moderatorName: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val reason: String,
    val previousState: String = "",
    val newState: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class RecoveryStatus(val label: String) {
    PENDING("Pending Verification"),
    VERIFIED("Identity Verified"),
    RESOLVED("Access Restored"),
    REJECTED("Rejected")
}

data class AccountRecoveryRequest(
    val id: String,
    val email: String,
    val username: String,
    val reason: String,
    val recoveryCode: String,
    val status: RecoveryStatus = RecoveryStatus.PENDING,
    val moderatorNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SessionInfo(
    val id: String,
    val deviceName: String,
    val ipAddress: String,
    val location: String,
    val isCurrentDevice: Boolean = false,
    val loginTimestamp: Long = System.currentTimeMillis()
)
