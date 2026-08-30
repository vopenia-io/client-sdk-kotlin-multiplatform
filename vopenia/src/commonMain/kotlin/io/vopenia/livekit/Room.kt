package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.video.VideoSubscribeQuality
import io.vopenia.livekit.permissions.Permission
import io.vopenia.livekit.permissions.PermissionRefused
import io.vopenia.livekit.permissions.PermissionsController
import io.vopenia.sdk.utils.Dispatchers
import io.vopenia.sdk.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Room {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val connectionStateEmitter: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Default)
    val connectionState: StateFlow<ConnectionState> = connectionStateEmitter.asStateFlow()

    internal val internalRoom = InternalRoom(
        scope,
        connectionStateEmitter
    )

    val remoteParticipants = internalRoom.remoteParticipants

    val localParticipant = internalRoom.localParticipant

    /**
     * Mirrors `LiveKitClient.Room.isRecording` — `true` while a server-side
     * recording (Meet transcript agent OR screen-recording Egress) is active.
     * Updated by the platform `RoomEvent.RecordingStatusChanged` /
     * `room:didUpdateIsRecording` hooks.
     */
    val isRecording: StateFlow<Boolean> get() = internalRoom.isRecording

    init {
        Log.d("Room", "creating room for sdk v0.0.9-alpha4")
    }

    suspend fun connect(
        url: String,
        token: String,
        enableMicrophone: Boolean = true
    ) {
        // Only require the microphone permission when the caller actually wants to
        // publish audio. A camera-only / listen-only join must be able to connect
        // without RECORD_AUDIO (Meet-web parity) instead of failing here — the
        // permission would otherwise be requested even for enableMicrophone = false.
        if (enableMicrophone) {
            if (!PermissionsController.isGranted(Permission.RECORD_AUDIO)) {
                PermissionsController.providePermission(Permission.RECORD_AUDIO)
            }

            if (!PermissionsController.isGranted(Permission.RECORD_AUDIO)) {
                throw PermissionRefused(Permission.RECORD_AUDIO)
            }
        }

        internalRoom.connect(url, token, enableMicrophone)
    }

    fun disconnect() {
        internalRoom.disconnect()
    }

    /**
     * Cap the receiving quality of every remote **camera** track. Screen-share
     * tracks are not capped. The cap is remembered so any camera that publishes
     * later in the call adopts it.
     */
    fun setMaxReceivingQuality(quality: VideoSubscribeQuality) {
        internalRoom.setMaxReceivingQuality(quality)
    }
}
