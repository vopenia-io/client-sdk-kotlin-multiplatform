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

    // A remote handle never denotes the same participant as a handle from the
    // other side of the call, so the type guard stays. The identity rule
    // itself — and hashCode — come from Participant.
    override fun equals(other: Any?): Boolean =
        other is RemoteParticipant && hasSameIdentityAs(other)
}
