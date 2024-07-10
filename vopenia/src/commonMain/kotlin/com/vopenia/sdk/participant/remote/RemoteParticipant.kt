package com.vopenia.sdk.participant.remote

import com.vopenia.sdk.participant.track.RemoteAudioTrack
import com.vopenia.sdk.participant.track.RemoteTrack
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import com.vopenia.sdk.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class RemoteParticipant(protected val scope: CoroutineScope) {
    protected val remoteTracks = MutableStateFlow<List<RemoteTrack>>(emptyList())
    protected val isSpeakingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val tracks: StateFlow<List<RemoteTrack>> = remoteTracks.asStateFlow()

    val videoTracks = remoteTracks.map(scope) { it.filterIsInstance<RemoteVideoTrack>() }
    val audioTracks = remoteTracks.map(scope) { it.filterIsInstance<RemoteAudioTrack>() }

    abstract val stateFlow: MutableStateFlow<RemoteParticipantState>

    abstract val identity: String?

    override fun equals(other: Any?): Boolean {
        if (other is RemoteParticipant) {
            return other.identity == identity
        }

        return false
    }

    val state: StateFlow<RemoteParticipantState>
        get() = stateFlow.asStateFlow()

    val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    protected fun append(track: RemoteTrack) {
        scope.launch {
            remoteTracks.emit(remoteTracks.value + track)
        }
    }

}