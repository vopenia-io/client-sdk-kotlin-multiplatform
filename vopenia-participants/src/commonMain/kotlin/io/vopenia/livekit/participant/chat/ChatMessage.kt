package io.vopenia.livekit.participant.chat

data class ChatMessage(
    val id: String,
    val timestamp: Long,
    val editTimestamp: Long? = null,
    val message: String,
    val deleted: Boolean = false,
    val generated: Boolean = false,
    val senderIdentity: String? = null
)
