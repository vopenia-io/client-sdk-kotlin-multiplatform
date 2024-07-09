package com.vopenia.sdk.participant.remote

import kotlinx.coroutines.flow.StateFlow

interface RemoteParticipant {
    val identity: String?
    val state: StateFlow<RemoteParticipantState>
    val isSpeakingState: StateFlow<Boolean>
}