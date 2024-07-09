package com.vopenia.sdk.participant.remote

import com.vopenia.sdk.participant.ParticipantPermissions

data class RemoteParticipantState(
    //val connectionQuality: ConnectionQuality
    val connected: Boolean,
    val metadata: String? = null,
    val name: String? = null,
    val permissions: ParticipantPermissions
)
