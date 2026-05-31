package io.vopenia.livekit

import LiveKitClientKotlin.LocalParticipantKotlin
import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.video.VideoSubscribeQuality
import io.vopenia.sdk.utils.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalForeignApi::class)
internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    private val roomDelegate = RoomDelegate(scope) {
        scope.launch {
            Log.d("InternalRoom", "launching new event $it")
            connectionStateEmitter.emit(it)
        }
    }

    actual suspend fun connect(
        url: String,
        token: String,
        enableMicrophone: Boolean
    ) {
        // first we reset the connection state
        connectionStateEmitter.emit(ConnectionState.Connecting)

        roomDelegate.connectWithUrl(url, token, enableMicrophone)
    }

    actual fun disconnect() {
        roomDelegate.disconnect()
    }

    actual val localParticipant: LocalParticipant
        get() = roomDelegate.localParticipant

    actual val remoteParticipants: StateFlow<List<RemoteParticipant>> =
        roomDelegate.remoteParticipants

    actual fun setMaxReceivingQuality(quality: VideoSubscribeQuality) {
        // Maps VideoSubscribeQuality ordinal (Low=0, Standard=1, High=2) to
        // LiveKit's VideoQuality enum on the Swift side. The Swift helper
        // iterates current camera publications; new publications get the cap
        // re-applied via RoomDelegate's didPublishTrack hook (TODO).
        LocalParticipantKotlin.setMaxCameraReceivingQualityWithRoom(
            room = roomDelegate.room,
            qualityRaw = quality.ordinal.toLong()
        ) { _ -> }
    }
}
