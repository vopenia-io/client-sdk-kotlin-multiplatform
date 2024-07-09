package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import com.vopenia.sdk.participant.InternalLocalParticipant
import com.vopenia.sdk.participant.InternalRemoteParticipant
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipant
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.livekit.android.room.participant.RemoteParticipant as RP

internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    private val room = LiveKit.create(Sdk.applicationContext)
    actual val localParticipant: LocalParticipant = InternalLocalParticipant(
        scope,
        room.localParticipant
    )

    private val participants = MutableStateFlow<List<InternalRemoteParticipant>>(emptyList())

    actual val remoteParticipants: StateFlow<List<RemoteParticipant>> = participants.asStateFlow()

    actual suspend fun connect(url: String, token: String) {
        // nothing for now
        collect()

        room.connect(url, token)

        room.remoteParticipants.values.forEach { onParticipantConnected(it) }

        room.localParticipant.setMicrophoneEnabled(true)
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

                is RoomEvent.ParticipantConnected -> onParticipantConnected(it.participant)
                is RoomEvent.ParticipantDisconnected -> onParticipantDisconnected(it.participant)
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

    actual fun disconnect() {
        room.disconnect()
    }


    private fun onParticipantConnected(participant: RP) {
        scope.launch {
            val list = participants.value

            val identity = participant.identity?.value

            println("Having onParticipantConnected ${participant.identity}")

            list.find { it.identity == identity }.let {
                println("found existing participant ${it}")
                if (null == it) {
                    val newParticipant = InternalRemoteParticipant(scope, participant, true)
                    newParticipant.onConnect()

                    participants.emit(list + newParticipant)
                }
            }
        }
    }

    private fun onParticipantDisconnected(participant: RP) {
        scope.launch {
            val identity = participant.identity?.value

            participants.value.find { it.identity == identity }?.onDisconnect()
        }
    }
}