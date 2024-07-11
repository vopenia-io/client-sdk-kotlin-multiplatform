package com.vopenia.sdk.participant.local

import com.vopenia.sdk.participant.ParticipantPermissions

data class LocalParticipantState(
    // val connectionQuality: ConnectionQuality
    val metadata: String? = null,
    val name: String? = null,
    val permissions: ParticipantPermissions
)
