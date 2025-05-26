package com.vopenia.livekit.participant

interface ParticipantState {
    val metadata: String?
    val name: String?
    val permissions: ParticipantPermissions
}
