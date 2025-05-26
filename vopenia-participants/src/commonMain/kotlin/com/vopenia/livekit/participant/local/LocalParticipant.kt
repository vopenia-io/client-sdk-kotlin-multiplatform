package com.vopenia.livekit.participant.local

import com.vopenia.livekit.participant.Participant
import com.vopenia.livekit.participant.track.local.LocalAudioTrack
import com.vopenia.livekit.participant.track.local.LocalTrack
import com.vopenia.livekit.participant.track.local.LocalVideoTrack
import kotlinx.coroutines.CoroutineScope

abstract class LocalParticipant(scope: CoroutineScope) :
    Participant<LocalTrack, LocalParticipantState, LocalAudioTrack, LocalVideoTrack>(scope) {
    abstract suspend fun enableMicrophone(enabled: Boolean)

    abstract suspend fun enableCamera(enabled: Boolean)

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
