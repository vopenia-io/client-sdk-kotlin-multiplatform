package io.vopenia.app.utils

import com.vopenia.sdk.participant.ParticipantPermissions
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import com.vopenia.sdk.participant.track.RemoteAudioTrack
import com.vopenia.sdk.participant.track.RemoteTrack
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import com.vopenia.sdk.utils.Dispatchers
import kotlinx.coroutines.CoroutineScope

class FakeRemoteParticipant : RemoteParticipant(
    scope = CoroutineScope(Dispatchers.Unconfined),
    RemoteParticipantState(
        connected = false,
        permissions = ParticipantPermissions()
    )
) {
    override fun filterListAudio(tracks: List<RemoteTrack>): List<RemoteAudioTrack> {
        return tracks.filterIsInstance<RemoteAudioTrack>()
    }

    override fun filterListVideo(tracks: List<RemoteTrack>): List<RemoteVideoTrack> {
        return tracks.filterIsInstance<RemoteVideoTrack>()
    }

    override val identity = "fake"
}
