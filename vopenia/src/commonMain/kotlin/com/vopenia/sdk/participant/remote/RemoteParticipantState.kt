package com.vopenia.sdk.participant.remote

import com.vopenia.sdk.participant.ParticipantPermissions
import com.vopenia.sdk.participant.ParticipantState

data class RemoteParticipantState(
    // val connectionQuality: ConnectionQuality
    val connected: Boolean,
    override val metadata: String? = null,
    override val name: String? = null,
    override val permissions: ParticipantPermissions
): ParticipantState
