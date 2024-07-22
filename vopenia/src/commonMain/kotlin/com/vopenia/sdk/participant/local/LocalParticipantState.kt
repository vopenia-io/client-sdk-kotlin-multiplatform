package com.vopenia.sdk.participant.local

import com.vopenia.sdk.participant.ParticipantPermissions
import com.vopenia.sdk.participant.ParticipantState

data class LocalParticipantState(
    // val connectionQuality: ConnectionQuality
    override val metadata: String? = null,
    override val name: String? = null,
    override val permissions: ParticipantPermissions
) : ParticipantState
