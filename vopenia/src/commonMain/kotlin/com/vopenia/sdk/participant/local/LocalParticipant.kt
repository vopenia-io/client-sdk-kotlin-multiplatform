package com.vopenia.sdk.participant.local

import kotlinx.coroutines.flow.StateFlow

interface LocalParticipant {
    val identity: String?
    val state: StateFlow<LocalParticipantState>
    val isSpeakingState: StateFlow<Boolean>

    suspend fun enableMicrophone(enabled: Boolean)
}