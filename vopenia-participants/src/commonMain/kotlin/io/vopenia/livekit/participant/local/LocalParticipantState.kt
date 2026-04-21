package io.vopenia.livekit.participant.local

import io.vopenia.livekit.participant.ParticipantPermissions
import io.vopenia.livekit.participant.ParticipantState

data class LocalParticipantState(
    // val connectionQuality: ConnectionQuality
    override val metadata: String? = null,
    override val name: String? = null,
    override val permissions: ParticipantPermissions
) : ParticipantState
