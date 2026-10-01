package com.example.model

enum class CallType { VOICE, VIDEO }
enum class CallStatus { ANSWERED, MISSED, REJECTED }

data class CallRecord(
    val id: String,
    val remoteUserId: String,
    val remoteUserName: String,
    val remoteUserAvatar: String = "",
    val callType: CallType = CallType.VOICE,
    val isIncoming: Boolean = false,
    val status: CallStatus = CallStatus.ANSWERED,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
