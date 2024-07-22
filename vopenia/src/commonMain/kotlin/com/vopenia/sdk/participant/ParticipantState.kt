package com.vopenia.sdk.participant

interface ParticipantState {
    val metadata: String?
    val name: String?
    val permissions: ParticipantPermissions
}
