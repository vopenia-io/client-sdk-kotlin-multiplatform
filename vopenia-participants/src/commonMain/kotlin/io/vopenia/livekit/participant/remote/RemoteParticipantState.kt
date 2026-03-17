package io.vopenia.livekit.participant.remote

import io.vopenia.livekit.participant.ParticipantPermissions
import io.vopenia.livekit.participant.ParticipantState

data class RemoteParticipantState(
    // val connectionQuality: ConnectionQuality
    val connected: Boolean,
    override val metadata: String? = null,
    override val name: String? = null,
    override val permissions: ParticipantPermissions
) : ParticipantState
