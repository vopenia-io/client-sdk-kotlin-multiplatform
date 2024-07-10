package com.vopenia.sdk.participant.remote

import com.vopenia.sdk.participant.track.RemoteTrack
import kotlinx.coroutines.flow.StateFlow

interface RemoteParticipant {
    val identity: String?
    val state: StateFlow<RemoteParticipantState>
    val tracks: StateFlow<List<RemoteTrack>>
    val isSpeakingState: StateFlow<Boolean>
}