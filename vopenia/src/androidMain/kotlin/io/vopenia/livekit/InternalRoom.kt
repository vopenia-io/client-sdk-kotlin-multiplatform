package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.InternalLocalParticipant
import io.vopenia.livekit.participant.InternalRemoteParticipant
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.video.VideoSubscribeQuality
import io.livekit.android.LiveKit
import io.livekit.android.annotations.Beta
import io.livekit.android.events.RoomEvent
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoQuality
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
    ).also { it.registerChatTextStream(room) }

    internal fun initVideoRenderer(textureViewRenderer: TextureViewRenderer) {
        room.initVideoRenderer(textureViewRenderer)
    }

    private val participants = MutableStateFlow<List<InternalRemoteParticipant>>(emptyList())

    actual val remoteParticipants: StateFlow<List<RemoteParticipant>> = participants.asStateFlow()

    private val isRecordingState = MutableStateFlow(false)
    actual val isRecording: StateFlow<Boolean> = isRecordingState.asStateFlow()

    actual suspend fun connect(url: String, token: String, enableMicrophone: Boolean) {
        // nothing for now
        collect()

        // first we reset the connection state
        connectionStateEmitter.emit(ConnectionState.Connecting)

        room.connect(url, token)

        room.remoteParticipants.values.forEach { onParticipantConnected(it) }

        if (enableMicrophone) {
            room.localParticipant.setMicrophoneEnabled(true)
        }
    }

    @OptIn(Beta::class)
    private fun collect() = scope.launch {
        room.events.events.collect {
            when (it) {
                // is RoomEvent.ActiveSpeakersChanged -> TODO()
                is RoomEvent.Connected -> connectionStateEmitter.emit(ConnectionState.Connected)
                // is RoomEvent.ConnectionQualityChanged -> TODO()
                // is RoomEvent.DataReceived -> TODO()
                is RoomEvent.Disconnected -> connectionStateEmitter.emit(ConnectionState.Disconnected)
                is RoomEvent.FailedToConnect -> connectionStateEmitter.emit(
                    ConnectionState.ConnectionError(
                        it.error
                    )
                )

                is RoomEvent.ParticipantConnected -> onParticipantConnected(it.participant)
                is RoomEvent.ParticipantDisconnected -> onParticipantDisconnected(it.participant)
                is RoomEvent.RecordingStatusChanged -> isRecordingState.emit(room.isRecording)
                is RoomEvent.TrackPublished -> {
                    // Re-apply the receiving-quality cap to any new camera track
                    // that arrives mid-call so it adopts the user's setting.
                    val pub = it.publication
                    if (pub is RemoteTrackPublication && pub.source == Track.Source.CAMERA) {
                        pub.setVideoQuality(receivingQuality.toLkVideoQuality())
                    }
                }
                // is RoomEvent.ParticipantMetadataChanged -> TODO()
                // is RoomEvent.ParticipantNameChanged -> TODO()
                // is RoomEvent.ParticipantPermissionsChanged -> TODO()
                is RoomEvent.Reconnected -> connectionStateEmitter.emit(ConnectionState.Connected)
                is RoomEvent.Reconnecting -> connectionStateEmitter.emit(ConnectionState.Connecting)
                // is RoomEvent.RecordingStatusChanged -> TODO()
                // is RoomEvent.RoomMetadataChanged -> TODO()
                // is RoomEvent.TrackE2EEStateEvent -> TODO()
                // is RoomEvent.TrackMuted -> TODO()
                // is RoomEvent.TrackPublished -> TODO()
                // is RoomEvent.TrackStreamStateChanged -> TODO()
                // is RoomEvent.TrackSubscribed -> TODO()
                // is RoomEvent.TrackSubscriptionFailed -> TODO()
                // is RoomEvent.TrackSubscriptionPermissionChanged -> TODO()
                // is RoomEvent.TrackUnmuted -> TODO()
                // is RoomEvent.TrackUnpublished -> TODO()
                // is RoomEvent.TrackUnsubscribed -> TODO()
                // is RoomEvent.TranscriptionReceived -> TODO()

                else -> {
                    // nothing
                }
            }
        }
    }

    actual fun disconnect() {
        room.disconnect()
    }

    @Volatile
    private var receivingQuality: VideoSubscribeQuality = VideoSubscribeQuality.High

    actual fun setMaxReceivingQuality(quality: VideoSubscribeQuality) {
        receivingQuality = quality
        val target = quality.toLkVideoQuality()
        room.remoteParticipants.values.forEach { participant ->
            participant.trackPublications.values
                .filterIsInstance<RemoteTrackPublication>()
                .filter { it.source == Track.Source.CAMERA }
                .forEach { it.setVideoQuality(target) }
        }
    }

    private fun VideoSubscribeQuality.toLkVideoQuality(): VideoQuality = when (this) {
        VideoSubscribeQuality.Low -> VideoQuality.LOW
        VideoSubscribeQuality.Standard -> VideoQuality.MEDIUM
        VideoSubscribeQuality.High -> VideoQuality.HIGH
    }

    private fun onParticipantConnected(participant: RP) {
        scope.launch {
            val list = participants.value

            val identity = participant.identity?.value

            println("Having onParticipantConnected ${participant.identity}")

            list.find { it.identity == identity }.let {
                println("found existing participant $it")
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
