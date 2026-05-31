package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.chat.ChatMessage
import io.vopenia.livekit.participant.devices.AudioInputDevice
import io.vopenia.livekit.participant.devices.CameraDevice
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.video.VideoResolutionPreset
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.local.LocalParticipantState
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

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

    override suspend fun enableMicrophone(enabled: Boolean, device: AudioInputDevice?) {
        // not available
    }

    override suspend fun enableCamera(enabled: Boolean, device: CameraDevice?) {
        // not available
    }

    override suspend fun switchCamera() {
        // not available
    }

    override suspend fun startScreenShare() {
        // not available
    }

    override suspend fun stopScreenShare() {
        // not available
    }

    override suspend fun setVideoEffect(effect: VideoEffect?) {
        // not available on JVM
    }

    override suspend fun setMaxSendingResolution(preset: VideoResolutionPreset) {
        // not available on JVM
    }

    override suspend fun publishData(payload: ByteArray, reliable: Boolean, topic: String?) {
        // not available
    }

    override suspend fun updateAttributes(attributes: Map<String, String>) {
        stateFlow.emit(stateFlow.value.copy(attributes = stateFlow.value.attributes + attributes))
    }

    override suspend fun sendChatMessage(text: String): ChatMessage =
        ChatMessage(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            message = text,
            senderIdentity = identity
        )

    override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> {
        return tracks.filterIsInstance<LocalAudioTrack>()
    }

    override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> {
        return tracks.filterIsInstance<LocalVideoTrack>()
    }
}
