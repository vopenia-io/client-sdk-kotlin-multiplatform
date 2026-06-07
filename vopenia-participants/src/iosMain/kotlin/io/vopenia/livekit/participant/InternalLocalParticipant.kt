package io.vopenia.livekit.participant

import LiveKitClient.setMicrophoneWithEnabled
import LiveKitClient.setScreenShareWithEnabled
import LiveKitClientKotlin.BbbaNoiseFilterKotlin
import LiveKitClientKotlin.DelegateKotlin
import LiveKitClientKotlin.LocalParticipantKotlin
import io.vopenia.livekit.participant.effects.BackgroundImage
import platform.Foundation.NSBundle
import platform.UIKit.UIImage
import io.vopenia.livekit.NSErrorException
import io.vopenia.livekit.participant.chat.ChatMessage
import io.vopenia.livekit.participant.chat.ChatMessageProto
import io.vopenia.livekit.participant.chat.ChatTopics
import io.vopenia.livekit.participant.data.DataPacket
import io.vopenia.livekit.participant.delegate.LocalParticipantDelegate
import io.vopenia.livekit.participant.devices.AudioInputDevice
import io.vopenia.livekit.participant.devices.CameraDevice
import io.vopenia.livekit.participant.effects.VideoEffect
import io.vopenia.livekit.participant.video.VideoResolutionPreset
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.local.LocalParticipantState
import io.vopenia.livekit.participant.track.Kind
import io.vopenia.livekit.participant.track.kindFrom
import io.vopenia.livekit.participant.track.local.LocalAudioTrack
import io.vopenia.livekit.participant.track.local.LocalNoneTrack
import io.vopenia.livekit.participant.track.local.LocalTrack
import io.vopenia.livekit.participant.track.local.LocalTrackPublication
import io.vopenia.livekit.participant.track.local.LocalVideoTrack
import io.vopenia.livekit.participant.transcription.TranscriptionSegment
import io.vopenia.livekit.permissions.Permission
import io.vopenia.livekit.permissions.PermissionsController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import LiveKitClient.LocalParticipant as LP

@OptIn(ExperimentalForeignApi::class)
class InternalLocalParticipant(
    scope: CoroutineScope,
    private val localParticipant: LP
) : LocalParticipant(scope) {
    private val delegateWrapper = DelegateKotlin()
    override val stateFlow = MutableStateFlow(
        LocalParticipantState(
            // Mirror InternalRemoteParticipant which seeds `name` at construction.
            // For the local participant the LiveKit iOS SDK does NOT fire
            // `onNameUpdated` for the token's initial value, so without this seed
            // (and the refreshNameFromNative() hook fired by RoomDelegate on
            // Connected) the local tile renders LiveKit's "Anonymous" identity.
            name = localParticipant.name(),
            permissions = InternalParticipantPermissions(
                localParticipant.permissions()
            ).toMultiplatform(),
            attributes = localParticipant.attributes() as? Map<String, String> ?: emptyMap()
        )
    )

    override val transcriptsFlow = MutableSharedFlow<TranscriptionSegment>()

    // Lazy getter — identity is assigned by the LiveKit server during connect,
    // which happens AFTER InternalLocalParticipant is created. A val captured at
    // construction time stays null forever. Re-reading on every access avoids this.
    override val identity: String?
        get() = localParticipant.identity()?.stringValue()

    // Track current camera position so switchCamera() can flip front<->back without
    // re-querying the underlying capturer (LiveKitClient does not expose it on LocalParticipant).
    private var currentCameraPosition: Long = AVCaptureDevicePositionFront

    private val delegate = delegateWrapper.wrapParticipantDelegateWithDelegate(
        LocalParticipantDelegate(
            onConnectionQuality = { _ -> },
            onIsSpeaking = { isSpeaking ->
                scope.async { isSpeakingFlow.emit(isSpeaking) }
            },
            onMetadataUpdated = { metadata ->
                scope.async { stateFlow.emit(stateFlow.value.copy(metadata = metadata)) }
            },
            onNameUpdated = { name ->
                scope.async { stateFlow.emit(stateFlow.value.copy(name = name)) }
            },
            onPermissionsUpdated = { permissions ->
                scope.async {
                    stateFlow.emit(
                        stateFlow.value.copy(
                            permissions = InternalParticipantPermissions(permissions).toMultiplatform()
                        )
                    )
                }
            },
            onAttributesUpdated = { attributes ->
                scope.async {
                    stateFlow.emit(stateFlow.value.copy(attributes = attributes))
                }
            },
            onTrackPublished = { track ->
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)
                wrapper.setPublished(true)
                if (new) append(wrapper)
            },
            onTrackUnpublished = { track ->
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)
                wrapper.setPublished(false)
                if (new) append(wrapper)
            },
            onTrackPublicationIsMuted = { track, isMuted ->
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)
                wrapper.setMuted(isMuted)
                if (new) append(wrapper)
            },
            onTranscriptionSegmentsReceived = { segments ->
                segments.forEach { transcriptsFlow.tryEmit(it) }
            },
            onDataReceived = { data, topic, senderIdentity ->
                scope.async {
                    val bytes = data.toByteArray()
                    dataReceivedFlowInternal.emit(DataPacket(bytes, topic, senderIdentity))
                    if (topic == ChatTopics.CHAT) {
                        runCatching { ChatMessageProto.decode(bytes, senderIdentity) }
                            .getOrNull()
                            ?.let { chatMessagesFlowInternal.emit(it) }
                    }
                }
            }
        )
    )

    init {
        delegateWrapper.appendToParticipant(localParticipant, delegate)
    }

    /**
     * Called from the Room delegate when ANY participant's attributes change
     * (including this local one). Routes the latest map into [stateFlow] so
     * `Room.handStates` and similar observers see updates from the server
     * — the Participant delegate alone doesn't reliably fire for the local
     * participant on iOS.
     */
    fun onAttributesUpdatedFromRoom(attributes: Map<String, String>) {
        scope.async {
            stateFlow.emit(stateFlow.value.copy(attributes = attributes))
        }
    }

    /**
     * Re-read the native `name` and publish it on [stateFlow]. Invoked by
     * RoomDelegate right after the LiveKit room reaches Connected, because
     * the iOS SDK doesn't fire `onNameUpdated` for the token's initial value
     * — without this, the local tile shows LiveKit's "Anonymous" identity.
     *
     * Synchronous on purpose: uses `update { ... }` (atomic read-modify-write)
     * rather than `scope.async { emit(...) }` so the new value is visible to
     * downstream observers before `emit(ConnectionState.Connected)` propagates,
     * avoiding an "Anonymous" flash on first render.
     */
    fun refreshNameFromNative() {
        stateFlow.update { it.copy(name = localParticipant.name()) }
    }

    /// Register the LiveKit Text Stream handler for chat ("lk.chat" topic).
    /// Called by [io.vopenia.livekit.RoomDelegate] after the Room is connected
    /// — we need a direct `LiveKitClient.Room` handle, which `LocalParticipant`
    /// doesn't expose publicly. This wires inbound chat messages from any
    /// remote participant into the local participant's `chatMessages` flow.
    fun registerChatTextStream(room: LiveKitClient.Room) {
        LocalParticipantKotlin.registerChatHandlerWithRoom(room = room) { text, senderIdentity ->
            val safeText = text ?: return@registerChatHandlerWithRoom
            println("[CHAT-iOS] text stream received: from=$senderIdentity text=\"$safeText\"")
            scope.async {
                chatMessagesFlowInternal.emit(
                    ChatMessage(
                        id = NSUUID().UUIDString(),
                        timestamp = (NSDate().timeIntervalSince1970() * 1000.0).toLong(),
                        message = safeText,
                        senderIdentity = senderIdentity
                    )
                )
            }
        }
    }

    override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> =
        tracks.filterIsInstance<LocalAudioTrack>()

    override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> =
        tracks.filterIsInstance<LocalVideoTrack>()

    // Tier (a) noise suppression — surface livered, WebRTC built-in flag.
    // Tier (b) RNNoise/Krisp processor attach is a follow-up.
    override val noiseReductionSupported: Boolean = true

    override suspend fun setNoiseReduction(enabled: Boolean) {
        println("[NOISE-iOS] setNoiseReduction($enabled)")
        noiseReductionEnabledState.value = enabled
        // RNNoise (BigBlueBetterAudio) capture-post processor. Installed lazily
        // on AudioManager.shared.capturePostProcessingDelegate and toggled live;
        // it is a pure pass-through while disabled, so no track re-publish.
        BbbaNoiseFilterKotlin.setEnabled(enabled)
    }

    override suspend fun enableMicrophone(enabled: Boolean, device: AudioInputDevice?) {
        PermissionsController.checkOrProvide(Permission.RECORD_AUDIO)
        // Route the AVAudioSession input first — LiveKit's AudioCaptureOptions
        // doesn't carry an explicit input choice on iOS, the platform session
        // does. Errors are non-fatal (we still try to enable).
        if (enabled && device != null) {
            runCatching {
                suspendCoroutine { continuation ->
                    LocalParticipantKotlin.setPreferredAudioInputWithUid(device.id) { error ->
                        if (null != error) continuation.resumeWithException(NSErrorException(error))
                        else continuation.resume(Unit)
                    }
                }
            }.onFailure { println("setPreferredAudioInput failed: $it") }
        }
        suspendCoroutine { continuation ->
            localParticipant.setMicrophoneWithEnabled(enabled, null, null) { _, error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(Unit)
            }
        }
    }

    override suspend fun enableCamera(enabled: Boolean, device: CameraDevice?) {
        PermissionsController.checkOrProvide(Permission.CAMERA)
        val position = when {
            device == null -> currentCameraPosition
            device.isFront -> AVCaptureDevicePositionFront
            else -> AVCaptureDevicePositionBack
        }
        currentCameraPosition = position
        suspendCoroutine { continuation ->
            LocalParticipantKotlin.setCameraEnabledWithParticipant(
                participant = localParticipant,
                enabled = enabled,
                position = position
            ) { error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(Unit)
            }
        }
    }

    override suspend fun switchCamera() {
        val newPosition = suspendCoroutine { continuation ->
            LocalParticipantKotlin.switchCameraWithParticipant(localParticipant) { position, error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(position)
            }
        }
        currentCameraPosition = newPosition
    }

    override suspend fun setMaxSendingResolution(preset: VideoResolutionPreset) {
        val (width, height) = when (preset) {
            VideoResolutionPreset.Low -> 320 to 180
            VideoResolutionPreset.Standard -> 640 to 360
            VideoResolutionPreset.High -> 1280 to 720
        }
        suspendCoroutine { continuation ->
            LocalParticipantKotlin.setCameraResolutionWithParticipant(
                participant = localParticipant,
                width = width,
                height = height
            ) { error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(Unit)
            }
        }
    }

    override suspend fun startScreenShare() {
        suspendCoroutine { continuation ->
            localParticipant.setScreenShareWithEnabled(true) { _, error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(Unit)
            }
        }
    }

    override suspend fun stopScreenShare() {
        suspendCoroutine { continuation ->
            localParticipant.setScreenShareWithEnabled(false) { _, error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(Unit)
            }
        }
    }

    override suspend fun setVideoEffect(effect: VideoEffect?) {
        println("[EFFECT-iOS] setVideoEffect(effect=$effect)")
        when (effect) {
            null, VideoEffect.BlurLight, VideoEffect.BlurStrong -> {
                val enable = effect != null
                println("[EFFECT-iOS] calling setBackgroundBlur(enable=$enable)")
                suspendCoroutine { continuation ->
                    LocalParticipantKotlin.setBackgroundBlurWithParticipant(
                        participant = localParticipant,
                        enabled = enable
                    ) { error ->
                        println("[EFFECT-iOS] setBackgroundBlur completed, error=$error")
                        if (null != error) continuation.resumeWithException(NSErrorException(error))
                        else continuation.resume(Unit)
                    }
                }
            }
            is VideoEffect.Background -> {
                val uiImage = loadUiImage(effect.image)
                if (uiImage == null) {
                    println("setVideoEffect: failed to load background image ${effect.image}")
                    return
                }
                suspendCoroutine { continuation ->
                    LocalParticipantKotlin.setBackgroundImageWithParticipant(
                        participant = localParticipant,
                        image = uiImage
                    ) { error ->
                        if (null != error) continuation.resumeWithException(NSErrorException(error))
                        else continuation.resume(Unit)
                    }
                }
            }
        }
    }

    private fun loadUiImage(image: BackgroundImage): UIImage? = when (image) {
        is BackgroundImage.Bundled -> UIImage.imageNamed(image.name)
        is BackgroundImage.Uri -> {
            val raw = image.uri
            val path = when {
                raw.startsWith("file://") -> raw.removePrefix("file://")
                raw.startsWith("/") -> raw
                else -> null
            }
            path?.let { UIImage(contentsOfFile = it) }
        }
    }

    override suspend fun publishData(payload: ByteArray, reliable: Boolean, topic: String?) {
        val nsData = payload.toNSData()
        suspendCoroutine { continuation ->
            LocalParticipantKotlin.publishDataWithParticipant(
                participant = localParticipant,
                data = nsData,
                reliable = reliable,
                topic = topic
            ) { error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(Unit)
            }
        }
    }

    override suspend fun updateAttributes(attributes: Map<String, String>) {
        @Suppress("UNCHECKED_CAST")
        suspendCoroutine { continuation ->
            LocalParticipantKotlin.setAttributesWithParticipant(
                participant = localParticipant,
                attributes = attributes as Map<Any?, *>,
                completionHandler = { error ->
                    if (null != error) continuation.resumeWithException(NSErrorException(error))
                    else continuation.resume(Unit)
                }
            )
        }
        // Don't trust the NSDictionary -> Map<String,String> cast (cinterop returns
        // Map<Any?, *> which doesn't cleanly downcast). Merge the passed-in
        // attributes onto our current cached state so handStates / similar
        // StateFlow consumers see an update immediately.
        stateFlow.emit(
            stateFlow.value.copy(
                attributes = stateFlow.value.attributes + attributes
            )
        )
    }

    override suspend fun sendChatMessage(text: String): ChatMessage {
        // Use LiveKit Text Streams (sendText for topic "lk.chat") — the API
        // consumed by @livekit/components-react useChat on the Meet Web side.
        val (streamId, timestamp) = suspendCoroutine<Pair<String?, Long>> { continuation ->
            LocalParticipantKotlin.sendChatTextWithParticipant(
                participant = localParticipant,
                text = text
            ) { id, ts, error ->
                if (null != error) continuation.resumeWithException(NSErrorException(error))
                else continuation.resume(id to ts)
            }
        }
        val message = ChatMessage(
            id = streamId ?: NSUUID().UUIDString(),
            timestamp = if (timestamp != 0L) timestamp else (NSDate().timeIntervalSince1970() * 1000.0).toLong(),
            message = text,
            senderIdentity = identity
        )
        chatMessagesFlowInternal.emit(message)
        return message
    }

    private fun getOrCreate(track: LocalTrackPublication): Pair<LocalTrack, Boolean> =
        internalTracks.value.find { it.sid == track.sid().stringValue() }.let {
            if (null != it) {
                it.updateInternalTrack(track)
                it to false
            } else {
                when (kindFrom(track.kind())) {
                    Kind.Audio -> LocalAudioTrack(scope, track)
                    Kind.Video -> LocalVideoTrack(scope, track)
                    Kind.None -> LocalNoneTrack(scope, track)
                } to true
            }
        }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val length = length.toInt()
    if (length == 0) return ByteArray(0)
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData.create(bytes = null, length = 0u)
    return usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
    }
}
