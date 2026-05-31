package io.vopenia.livekit.participant

import io.vopenia.livekit.participant.chat.ChatMessage
import io.vopenia.livekit.participant.chat.ChatMessageProto
import io.vopenia.livekit.participant.chat.ChatTopics
import io.vopenia.livekit.participant.data.DataPacket
import io.vopenia.livekit.participant.devices.AudioInputDevice
import io.vopenia.livekit.participant.devices.CameraDevice
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.video.VideoResolutionPreset
import io.vopenia.livekit.effects.VideoEffectProcessor
import io.vopenia.livekit.effects.VideoProcessorAttacher
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.local.LocalParticipantState
import io.vopenia.livekit.screenshare.ScreenShareController
import io.livekit.android.room.track.screencapture.ScreenCaptureParams
import io.vopenia.livekit.participant.track.Kind
import io.vopenia.livekit.participant.track.kindFrom
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalNoneTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalTrackPublication
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import io.vopenia.livekit.participant.track.toLocalTranscriptionSegment
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import io.vopenia.livekit.permissions.Permission
import io.vopenia.livekit.permissions.PermissionsController
import io.vopenia.sdk.utils.Log
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.datastream.StreamTextOptions
import io.livekit.android.room.datastream.TextStreamInfo
import io.livekit.android.room.participant.VideoTrackPublishOptions
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.VideoPreset169
import io.vopenia.livekit.Sdk
import io.vopenia.livekit.participant.track.Source
import io.vopenia.livekit.participant.track.toLkSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import livekit.org.webrtc.Camera2Capturer
import java.util.UUID
import io.livekit.android.room.participant.LocalParticipant as LP
import io.livekit.android.room.track.LocalVideoTrack as LkLocalVideoTrack

class InternalLocalParticipant(
    scope: CoroutineScope,
    private val localParticipant: LP
) : LocalParticipant(scope) {
    override val stateFlow: MutableStateFlow<LocalParticipantState> = MutableStateFlow(
        LocalParticipantState(
            permissions = localParticipant.permissions?.let {
                InternalParticipantPermissions(it).toMultiplatform()
            } ?: ParticipantPermissions(),
            attributes = localParticipant.attributes
        )
    )
    override val transcriptsFlow = MutableSharedFlow<TranscriptionSegment>()

    // Lazy getter — identity is assigned by the LiveKit server during connect,
    // which happens AFTER InternalLocalParticipant is created.
    override val identity: String?
        get() = localParticipant.identity?.value

    init {
        scope.launch {
            localParticipant.events.collect {
                when (it) {
                    is ParticipantEvent.DataReceived -> {
                        handleDataReceived(it.data, it.topic, it.participant.identity?.value)
                    }

                    is ParticipantEvent.LocalTrackPublished -> {
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.LocalTrackUnpublished -> {
                        val (wrapper, new) = getOrCreate(it.publication)

                        wrapper.setPublished(false)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.MetadataChanged -> {
                        stateFlow.emit(stateFlow.value.copy(metadata = it.prevMetadata))
                    }

                    is ParticipantEvent.NameChanged -> {
                        stateFlow.emit(stateFlow.value.copy(name = it.name))
                    }

                    is ParticipantEvent.ParticipantPermissionsChanged -> {
                        it.newPermissions?.let { permissions ->
                            stateFlow.emit(
                                stateFlow.value.copy(
                                    permissions = InternalParticipantPermissions(
                                        permissions
                                    ).toMultiplatform()
                                )
                            )
                        }
                    }

                    is ParticipantEvent.SpeakingChanged -> {
                        isSpeakingFlow.emit(it.isSpeaking)
                    }

                    is ParticipantEvent.TrackMuted -> {
                        Log.d("LocalParticipant", "track is muted")
                        val (wrapper, new) = getOrCreate(it.publication as LocalTrackPublication)

                        wrapper.setMuted(true)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackPublished -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackStreamStateChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscribed -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscriptionFailed -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackSubscriptionPermissionChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackUnmuted -> {
                        val (wrapper, new) = getOrCreate(it.publication as LocalTrackPublication)

                        wrapper.setMuted(false)
                        if (new) append(wrapper)
                    }

                    is ParticipantEvent.TrackUnpublished -> {
                        // TODO
                    }

                    is ParticipantEvent.TrackUnsubscribed -> {
                        // TODO
                    }

                    is ParticipantEvent.AttributesChanged -> {
                        stateFlow.emit(stateFlow.value.copy(attributes = localParticipant.attributes))
                    }

                    is ParticipantEvent.LocalTrackPublicationFailed -> {
                        // TODO
                    }

                    is ParticipantEvent.LocalTrackSubscribed -> {
                        // TODO
                    }

                    is ParticipantEvent.StateChanged -> {
                        // TODO
                    }

                    is ParticipantEvent.TranscriptionReceived -> {
                        it.transcriptions.forEach { transcript ->
                            transcriptsFlow.emit(transcript.toLocalTranscriptionSegment())
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleDataReceived(
        data: ByteArray,
        topic: String?,
        senderIdentity: String?
    ) {
        dataReceivedFlowInternal.emit(DataPacket(data, topic, senderIdentity))
        if (topic == ChatTopics.CHAT) {
            runCatching { ChatMessageProto.decode(data, senderIdentity) }
                .getOrNull()
                ?.let { chatMessagesFlowInternal.emit(it) }
        }
    }

    // Registers a LiveKit Text Stream handler for the `lk.chat` topic — the API
    // used by `@livekit/components-react` `useChat()` (Meet Web) and by the iOS
    // SDK. Called from `InternalRoom` after the LiveKit Room is created, since
    // the receiver lives at Room scope, not Participant scope.
    fun registerChatTextStream(room: Room) {
        room.registerTextStreamHandler(CHAT_TEXT_STREAM_TOPIC) { receiver, fromIdentity ->
            scope.launch {
                runCatching { receiver.readAll().joinToString("") }
                    .onSuccess { text ->
                        chatMessagesFlowInternal.emit(
                            ChatMessage(
                                id = receiver.info.id,
                                timestamp = receiver.info.timestampMs,
                                message = text,
                                senderIdentity = fromIdentity.value,
                            )
                        )
                    }
            }
        }
    }

    // Tier (a) noise suppression — surface livered, WebRTC built-in flag.
    // Tier (b) RNNoise/Krisp processor attach is a follow-up.
    override val noiseReductionSupported: Boolean = true

    override suspend fun setNoiseReduction(enabled: Boolean) {
        Log.d("LocalParticipant", "setNoiseReduction($enabled)")
        noiseReductionEnabledState.value = enabled
        // To take effect on the live mic, the audio track has to be
        // re-published with the new LocalAudioTrackOptions. V1 leaves
        // that to the caller (toggle mic off then on, or reconnect to
        // the room). Wiring a hot-swap processor lands with the RNNoise
        // port (BigBlueBetterAudio).
    }

    override suspend fun enableMicrophone(enabled: Boolean, device: AudioInputDevice?) {
        PermissionsController.checkOrProvide(Permission.RECORD_AUDIO)
        Log.d("LocalParticipant", "enableMicrophone($enabled, device=${device?.id})")
        // LiveKit Android's setMicrophoneEnabled doesn't accept a device — the
        // capture always uses the system's "communication" input. Route the
        // choice through AudioManager.setCommunicationDevice (API 31+) before
        // enabling so the new track captures from the desired input.
        if (enabled && device != null) {
            applyCommunicationDevice(device)
        }
        localParticipant.setMicrophoneEnabled(enabled)
    }

    private fun applyCommunicationDevice(device: AudioInputDevice): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            // setCommunicationDevice was added in API 31. Older platforms only
            // support `setSpeakerphoneOn` / `startBluetoothSco` — not useful for
            // input routing. Log and bail.
            Log.d("LocalParticipant", "applyCommunicationDevice: API ${android.os.Build.VERSION.SDK_INT} < 31, skipping")
            return false
        }
        val context = runCatching { io.vopenia.livekit.Sdk.applicationContext }.getOrNull()
            ?: return false
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE)
            as? android.media.AudioManager ?: return false
        val target = audioManager.availableCommunicationDevices.firstOrNull {
            it.id.toString() == device.id
        } ?: run {
            Log.d("LocalParticipant", "applyCommunicationDevice: no input matching id=${device.id}")
            return false
        }
        return audioManager.setCommunicationDevice(target)
    }

    override suspend fun enableCamera(enabled: Boolean, device: CameraDevice?) {
        PermissionsController.checkOrProvide(Permission.CAMERA)
        localParticipant.setCameraEnabled(enabled)
        if (enabled && device != null) {
            findCameraTrack()?.switchCamera(device.id, null)
        }
    }

    override suspend fun switchCamera() {
        findCameraTrack()?.switchCamera(null, null)
    }

    // Remembered so a camera track published later in the call adopts the
    // user's choice without an explicit re-call. Defaults to Standard (~360p).
    @Volatile
    private var sendingResolutionPreset: VideoResolutionPreset = VideoResolutionPreset.Standard

    override suspend fun setMaxSendingResolution(preset: VideoResolutionPreset) {
        sendingResolutionPreset = preset
        val track = findCameraTrack() ?: return
        val current = track.options
        val updated = LocalVideoTrackOptions(
            isScreencast = current.isScreencast,
            deviceId = current.deviceId,
            position = current.position,
            captureParams = preset.toLkPreset().capture
        )
        track.restartTrack(updated)
    }

    private fun VideoResolutionPreset.toLkPreset(): VideoPreset169 = when (this) {
        VideoResolutionPreset.Low -> VideoPreset169.H180
        VideoResolutionPreset.Standard -> VideoPreset169.H360
        VideoResolutionPreset.High -> VideoPreset169.H720
    }

    private fun findCameraTrack(): io.livekit.android.room.track.LocalVideoTrack? {
        return localParticipant.trackPublications.values
            .asSequence()
            .filter { it.source == io.livekit.android.room.track.Track.Source.CAMERA }
            .mapNotNull { it.track as? io.livekit.android.room.track.LocalVideoTrack }
            .firstOrNull()
    }

    override suspend fun publishData(payload: ByteArray, reliable: Boolean, topic: String?) {
        val reliability = if (reliable) DataPublishReliability.RELIABLE else DataPublishReliability.LOSSY
        localParticipant.publishData(payload, reliability, topic, null)
    }

    override suspend fun updateAttributes(attributes: Map<String, String>) {
        localParticipant.updateAttributes(attributes)
        stateFlow.emit(stateFlow.value.copy(attributes = localParticipant.attributes))
    }

    override suspend fun startScreenShare() {
        val (intent, notification, notificationId) = ScreenShareController.consume()
            ?: throw IllegalStateException(
                "ScreenShareController.setMediaProjectionResult(...) must be called " +
                "with a MediaProjection result Intent before startScreenShare()."
            )
        localParticipant.setScreenShareEnabled(
            true,
            ScreenCaptureParams(intent, notificationId, notification, null)
        )
    }

    override suspend fun stopScreenShare() {
        localParticipant.setScreenShareEnabled(false, null)
    }

    // ------------------------------------------------------------------
    // Additive Android-only API: publish a Camera2 logical camera id as a
    // LiveKit video track with a caller-chosen source. Originally added
    // for Neat hardware where camera id "1" is the HDMI capture input and
    // we want to publish it with `Source.SCREEN_SHARE` so remote peers
    // render it as content rather than a camera feed. Generic enough to
    // be useful for any consumer that already has a logical camera id
    // and wants explicit control over the published track Source.
    //
    // Not part of the common LocalParticipant abstract — Android-only by
    // design. Public parameter is the vopenia `Source` enum so callers
    // don't pull in the LiveKit-Android dependency directly. Callers
    // cast `(room.localParticipant as? InternalLocalParticipant)` and
    // check for non-null before calling.
    // ------------------------------------------------------------------

    private val cameraSourcedTracks = mutableMapOf<String, Pair<LkLocalVideoTrack, Camera2Capturer>>()

    /**
     * Publish a Camera2 logical camera as a LiveKit video track with the
     * given [source]. Idempotent per [cameraId]: if a previous track was
     * published for the same id, it's torn down first.
     *
     * @param cameraId Camera2 logical id (e.g. `"0"` for AI camera, `"1"`
     *   for HDMI input on Neat hardware).
     * @param source vopenia [Source] enum — typically `SCREEN_SHARE` for
     *   content-share semantics; can be `CAMERA` to publish as an
     *   additional camera feed.
     * @param trackName Optional name for the track. Defaults to a stable
     *   `"camera-<id>-<source>"` form.
     */
    suspend fun publishVideoTrackFromCamera(
        cameraId: String,
        source: Source,
        trackName: String = "camera-$cameraId-${source.name.lowercase()}",
    ) {
        unpublishVideoTrackFromCamera(cameraId)
        val capturer = Camera2Capturer(Sdk.applicationContext, cameraId, null)
        val track = localParticipant.createVideoTrack(
            name = trackName,
            capturer = capturer,
            options = LocalVideoTrackOptions(),
            videoProcessor = null,
        )
        track.startCapture()
        localParticipant.publishVideoTrack(
            track = track,
            options = VideoTrackPublishOptions(source = source.toLkSource()),
        )
        cameraSourcedTracks[cameraId] = track to capturer
    }

    /**
     * Stop and unpublish a track previously published via
     * [publishVideoTrackFromCamera]. No-op for an unknown [cameraId].
     */
    suspend fun unpublishVideoTrackFromCamera(cameraId: String) {
        val (track, capturer) = cameraSourcedTracks.remove(cameraId) ?: return
        runCatching { localParticipant.unpublishTrack(track, true) }
        runCatching { capturer.stopCapture() }
    }

    private val videoEffectProcessor: VideoEffectProcessor by lazy { VideoEffectProcessor() }

    override suspend fun setVideoEffect(effect: VideoEffect?) {
        val cameraTrack = findCameraTrack()
        if (cameraTrack == null) {
            Log.d("LocalParticipant", "setVideoEffect: no camera track to attach to — start the camera first")
            return
        }
        videoEffectProcessor.currentEffect = effect
        val attached = VideoProcessorAttacher.attach(
            cameraTrack,
            if (effect == null) null else videoEffectProcessor
        )
        if (!attached) {
            Log.d("LocalParticipant", "setVideoEffect: failed to attach processor (reflection)")
        }
    }

    override suspend fun sendChatMessage(text: String): ChatMessage {
        // Use LiveKit Text Streams (lk.chat topic) — the API consumed by
        // `@livekit/components-react` `useChat()` on the Meet Web side and by
        // the iOS SDK. LiveKit doesn't echo Text Stream sends back to the
        // publisher, so we emit the local copy explicitly below.
        val sender = localParticipant.streamText(
            StreamTextOptions(
                topic = CHAT_TEXT_STREAM_TOPIC,
                operationType = TextStreamInfo.OperationType.CREATE,
            )
        )
        sender.write(text)
        sender.close()
        val message = ChatMessage(
            id = sender.info.id,
            timestamp = sender.info.timestampMs,
            message = text,
            senderIdentity = identity,
        )
        chatMessagesFlowInternal.emit(message)
        return message
    }

    override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> {
        return tracks.filterIsInstance<LocalAudioTrack>()
    }

    override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> {
        return tracks.filterIsInstance<LocalVideoTrack>()
    }

    private fun getOrCreate(
        track: LocalTrackPublication
    ): Pair<LocalTrack, Boolean> {
        Log.d("LOCAL", "getOrCreate for ${track.sid}")

        return internalTracks.value.find { it.sid == track.sid }.let {
            if (null != it) {
                it.updateInternalTrack(track)
                it to false
            } else {
                when (kindFrom(track.kind)) {
                    Kind.Audio -> LocalAudioTrack(scope, track)
                    Kind.Video -> LocalVideoTrack(scope, track)
                    Kind.None -> LocalNoneTrack(scope, track)
                } to true
            }
        }
    }
}

// LiveKit Text Streams topic for chat — used by `@livekit/components-react useChat()` (Meet Web)
// and the iOS SDK. Distinct from the legacy data-channel topic [ChatTopics.CHAT] which is still
// decoded inbound for backward compatibility with older Android builds.
private const val CHAT_TEXT_STREAM_TOPIC = "lk.chat"
