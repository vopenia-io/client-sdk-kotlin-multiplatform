package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

actual class Room actual constructor() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val room = LiveKit.create(Sdk.applicationContext)

    private val connectionStateEmitter: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Default)
    actual val connectionState: StateFlow<ConnectionState> = connectionStateEmitter.asStateFlow()

    actual suspend fun connect(url: String, token: String) {
        // nothing for now
        collect()

        room.connect(url, token)
    }

    private fun collect() = scope.launch {
        room.events.events.collect {
            when (it) {
                //is RoomEvent.ActiveSpeakersChanged -> TODO()
                is RoomEvent.Connected -> connectionStateEmitter.emit(ConnectionState.Connected)
                //is RoomEvent.ConnectionQualityChanged -> TODO()
                //is RoomEvent.DataReceived -> TODO()
                is RoomEvent.Disconnected -> connectionStateEmitter.emit(ConnectionState.Disconnected)
                is RoomEvent.FailedToConnect -> connectionStateEmitter.emit(
                    ConnectionState.ConnectionError(
                        it.error
                    )
                )
                //is RoomEvent.ParticipantConnected -> TODO()
                //is RoomEvent.ParticipantDisconnected -> TODO()
                //is RoomEvent.ParticipantMetadataChanged -> TODO()
                //is RoomEvent.ParticipantNameChanged -> TODO()
                //is RoomEvent.ParticipantPermissionsChanged -> TODO()
                is RoomEvent.Reconnected -> connectionStateEmitter.emit(ConnectionState.Connected)
                is RoomEvent.Reconnecting -> connectionStateEmitter.emit(ConnectionState.Connecting)
                //is RoomEvent.RecordingStatusChanged -> TODO()
                //is RoomEvent.RoomMetadataChanged -> TODO()
                //is RoomEvent.TrackE2EEStateEvent -> TODO()
                //is RoomEvent.TrackMuted -> TODO()
                //is RoomEvent.TrackPublished -> TODO()
                //is RoomEvent.TrackStreamStateChanged -> TODO()
                //is RoomEvent.TrackSubscribed -> TODO()
                //is RoomEvent.TrackSubscriptionFailed -> TODO()
                //is RoomEvent.TrackSubscriptionPermissionChanged -> TODO()
                //is RoomEvent.TrackUnmuted -> TODO()
                //is RoomEvent.TrackUnpublished -> TODO()
                //is RoomEvent.TrackUnsubscribed -> TODO()
                else -> {
                    // nothing
                }
            }
        }
    }
}