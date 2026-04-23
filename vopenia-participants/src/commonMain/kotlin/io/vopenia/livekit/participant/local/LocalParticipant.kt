package io.vopenia.livekit.participant.local

import io.vopenia.livekit.participant.Participant
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import kotlinx.coroutines.CoroutineScope

abstract class LocalParticipant(scope: CoroutineScope) :
    Participant<LocalTrack, LocalParticipantState, LocalAudioTrack, LocalVideoTrack>(scope) {
    abstract suspend fun enableMicrophone(enabled: Boolean)

    abstract suspend fun enableCamera(enabled: Boolean)

    abstract suspend fun publishData(
        data: ByteArray,
        reliable: Boolean = true,
        topic: String? = null,
        destinationIdentities: List<String>? = null,
    )

    override fun equals(other: Any?): Boolean {
        if (other is LocalParticipant) {
            return other.identity == identity
        }

        return false
    }

    override fun hashCode(): Int {
        return state.value.hashCode()
    }
}
