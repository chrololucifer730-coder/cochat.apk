package com.example.model

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION, CONTACT, POLL, STICKER
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class PollOption(
    val id: String,
    val text: String,
    val votes: List<String> = emptyList() // userIds
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val mediaSizeText: String = "",
    val mediaDurationSec: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val pollQuestion: String = "",
    val pollOptions: List<PollOption> = emptyList(),
    val replyToMessageId: String? = null,
    val replyToContent: String? = null,
    val replyToSender: String? = null,
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val isStarred: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val deletedForUserIds: List<String> = emptyList(),
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> userIds
    val status: MessageStatus = MessageStatus.SENT,
    val disappearsAt: Long? = null
)
