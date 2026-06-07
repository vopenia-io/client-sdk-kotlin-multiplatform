package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.InternalLocalParticipant
import io.vopenia.livekit.participant.InternalRemoteParticipant
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.livekit.participant.video.VideoSubscribeQuality
import io.vopenia.sdk.utils.Log
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.AudioOptions
import io.vopenia.livekit.audio.BbbaNoiseReduction
import io.livekit.android.annotations.Beta
import io.livekit.android.events.RoomEvent
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.RemoteTrackPublication
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoQuality
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.livekit.android.room.participant.RemoteParticipant as RP

internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    // Register the BigBlueBetterAudio capture-post noise processor at Room
    // creation (the only point where audio processing can be injected). It is
    // a pass-through until LocalParticipant.setNoiseReduction(true) enables it.
    private val room = LiveKit.create(
        Sdk.applicationContext,
        overrides = LiveKitOverrides(
            audioOptions = AudioOptions(
                audioProcessorOptions = BbbaNoiseReduction.audioProcessorOptions()
            )
        )
    )
    actual val localParticipant: LocalParticipant = InternalLocalParticipant(
        scope,
        room.localParticipant
    ).also { it.registerChatTextStream(room) }

    internal fun initVideoRenderer(textureViewRenderer: TextureViewRenderer) {
        room.initVideoRenderer(textureViewRenderer)
    }

    private val participants = MutableStateFlow<List<InternalRemoteParticipant>>(emptyList())

    // Guards the read-check-create-emit on [participants]: connect() fans
    // onParticipantConnected() out concurrently for every pre-existing
    // participant, and the dispatcher is multi-threaded.
    private val participantsMutex = Mutex()

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
            // A moderator may have revoked this participant's right to publish the
            // microphone (can_publish_sources). LiveKit then throws PublishException
            // ("insufficient permissions"). Catch it so a restricted participant still
            // joins (muted; the UI greys the mic control) instead of crashing the
            // connect coroutine. Re-throw cancellation so it isn't masked.
            try {
                room.localParticipant.setMicrophoneEnabled(true)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Log.d("InternalRoom", "Microphone not enabled on connect: ${error.message}")
            }
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
            val identity = participant.identity?.value

            // Read-check-create-emit must be atomic as a unit. connect() fans this
            // out concurrently for every pre-existing participant on a multi-thread
            // dispatcher; a plain `participants.value` read + `emit(list + new)`
            // races — two coroutines read the same list and the last emit wins, so
            // a participant is silently dropped from the grid (or, when the same
            // identity is processed twice, added in duplicate).
            participantsMutex.withLock {
                val list = participants.value
                if (list.none { it.identity == identity }) {
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
