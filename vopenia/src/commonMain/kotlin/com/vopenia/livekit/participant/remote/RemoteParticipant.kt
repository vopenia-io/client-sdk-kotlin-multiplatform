package com.vopenia.livekit.participant.remote

import com.vopenia.livekit.participant.Participant
import com.vopenia.livekit.participant.track.RemoteAudioTrack
import com.vopenia.livekit.participant.track.RemoteTrack
import com.vopenia.livekit.participant.track.RemoteVideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

abstract class RemoteParticipant(
    scope: CoroutineScope,
    defaultState: RemoteParticipantState
) : Participant<RemoteTrack, RemoteParticipantState, RemoteAudioTrack, RemoteVideoTrack>(scope) {
    override val stateFlow = MutableStateFlow(defaultState)

    override fun equals(other: Any?): Boolean {
        if (other is RemoteParticipant) {
            return other.identity == identity
        }

        return false
    }

    override fun hashCode(): Int {
        return state.value.hashCode()
    }
}
