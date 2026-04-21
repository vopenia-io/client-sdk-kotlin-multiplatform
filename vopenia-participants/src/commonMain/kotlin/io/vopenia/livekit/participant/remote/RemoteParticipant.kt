package io.vopenia.livekit.participant.remote

import io.vopenia.livekit.participant.Participant
import io.vopenia.livekit.participant.track.RemoteAudioTrack
import io.vopenia.livekit.participant.track.RemoteTrack
import io.vopenia.livekit.participant.track.RemoteVideoTrack
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class RemoteParticipant(
    scope: CoroutineScope,
    defaultState: RemoteParticipantState
) : Participant<RemoteTrack, RemoteParticipantState, RemoteAudioTrack, RemoteVideoTrack>(scope) {
    override val stateFlow = MutableStateFlow(defaultState)

    override val transcriptsFlow =MutableSharedFlow<TranscriptionSegment>()

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
