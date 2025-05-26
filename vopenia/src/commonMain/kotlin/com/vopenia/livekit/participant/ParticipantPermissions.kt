package com.vopenia.livekit.participant

data class ParticipantPermissions(
    val isHidden: Boolean = true,
    val isRecorder: Boolean = false,
    val canPublish: Boolean = false,
    val canPublishData: Boolean = false,
    val canSubscribe: Boolean = false
)
