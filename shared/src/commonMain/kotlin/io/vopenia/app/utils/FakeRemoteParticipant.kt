package io.vopenia.app.utils

import com.vopenia.livekit.participant.ParticipantPermissions
import com.vopenia.livekit.participant.remote.RemoteParticipant
import com.vopenia.livekit.participant.remote.RemoteParticipantState
import com.vopenia.livekit.participant.track.RemoteAudioTrack
import com.vopenia.livekit.participant.track.RemoteTrack
import com.vopenia.livekit.participant.track.RemoteVideoTrack
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
