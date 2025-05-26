package com.vopenia.livekit.participant.local

import com.vopenia.livekit.participant.ParticipantPermissions
import com.vopenia.livekit.participant.ParticipantState

data class LocalParticipantState(
    // val connectionQuality: ConnectionQuality
    override val metadata: String? = null,
    override val name: String? = null,
    override val permissions: ParticipantPermissions
) : ParticipantState
