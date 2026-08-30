package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.video.VideoSubscribeQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal expect class InternalRoom(
    scope: CoroutineScope,
    connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    val localParticipant: LocalParticipant

    val remoteParticipants: StateFlow<List<RemoteParticipant>>

    /**
     * `true` when the underlying LiveKit room reports an active server-side
     * recording (Meet's transcript agent or screen-recording Egress).
     * Reflects `Room.isRecording` natively — the SDK does not infer it.
     */
    val isRecording: StateFlow<Boolean>

    suspend fun connect(url: String, token: String, enableMicrophone: Boolean = true)

    fun disconnect()

    /**
     * Cap the receiving quality of every remote **camera** track currently
     * published in the room, and remember the cap so any track that publishes
     * later in the call adopts it. Screen-share tracks are intentionally not
     * capped (the user wants them sharp).
     */
    fun setMaxReceivingQuality(quality: VideoSubscribeQuality)
}
