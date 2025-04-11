package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import com.vopenia.sdk.participant.ParticipantPermissions
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.local.LocalParticipantState
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.track.local.LocalAudioTrack
import com.vopenia.sdk.participant.track.local.LocalTrack
import com.vopenia.sdk.participant.track.local.LocalVideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("UnusedPrivateMember")
internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    actual suspend fun connect(url: String, token: String) {
        // nothing for now
    }

    actual fun disconnect() {
        // nothing for now
    }

    actual val localParticipant: LocalParticipant
        get() = object : LocalParticipant(scope) {
            override val stateFlow = MutableStateFlow<LocalParticipantState>(
                LocalParticipantState(
                    permissions = ParticipantPermissions()
                )
            )

            override val identity: String?
                get() = "Not yet implemented"

            override suspend fun enableMicrophone(enabled: Boolean) {
                // not available
            }

            override suspend fun enableCamera(enabled: Boolean) {
                // not available
            }

            override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> {
                return tracks.filterIsInstance<LocalAudioTrack>()
            }

            override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> {
                return tracks.filterIsInstance<LocalVideoTrack>()
            }
        }

    private val remoteParticipantsState = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    actual val remoteParticipants: StateFlow<List<RemoteParticipant>>
        get() = remoteParticipantsState.asStateFlow()
}
