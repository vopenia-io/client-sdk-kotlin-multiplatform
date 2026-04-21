package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.local.LocalParticipantState
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class InternalLocalParticipant(
    scope: CoroutineScope
) : LocalParticipant(scope) {
    override val stateFlow = MutableStateFlow<LocalParticipantState>(
        LocalParticipantState(
            permissions = ParticipantPermissions()
        )
    )
    override val transcriptsFlow = MutableSharedFlow<TranscriptionSegment>()

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
