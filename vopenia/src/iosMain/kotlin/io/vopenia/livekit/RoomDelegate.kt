package io.vopenia.livekit

import LiveKitClient.ConnectOptions
import LiveKitClient.RemoteParticipant
import LiveKitClient.Room
import LiveKitClient.RoomDelegateProtocol
import LiveKitClient.RoomOptions
import LiveKitClientKotlin.DelegateKotlin
import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.InternalLocalParticipant
import io.vopenia.livekit.participant.InternalRemoteParticipant
import io.vopenia.livekit.room.RoomDelegateConnectionState
import io.vopenia.sdk.utils.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class RoomDelegate(
    private val scope: CoroutineScope,
    private val emit: (ConnectionState) -> Unit,
    private val onIsRecordingUpdated: (Boolean) -> Unit
) {
    private val delegateWrapper = DelegateKotlin()
    private val connectOptions = ConnectOptions()
    // suspendLocalVideoTracksInBackground defaults to true, which makes LiveKit
    // suspend (mute) the local camera when the app backgrounds — so on iOS the caller
    // goes mute for everyone else while the call floats in Picture-in-Picture. Turn it
    // off so the camera keeps publishing; the iOS app additionally enables the
    // AVCaptureSession's multitasking-camera access so the hardware isn't interrupted.
    //
    // The sub-options are REUSED from a default RoomOptions() (built in Swift) rather
    // than constructed here: calling ScreenShareCaptureOptions()/CameraCaptureOptions()
    // etc. from Kotlin/Native hits unimplemented initializers and crashes at runtime.
    private val defaultRoomOptions = RoomOptions()
    private val roomOptions = RoomOptions(
        defaultCameraCaptureOptions = defaultRoomOptions.defaultCameraCaptureOptions(),
        defaultScreenShareCaptureOptions = defaultRoomOptions.defaultScreenShareCaptureOptions(),
        defaultAudioCaptureOptions = defaultRoomOptions.defaultAudioCaptureOptions(),
        defaultVideoPublishOptions = defaultRoomOptions.defaultVideoPublishOptions(),
        defaultAudioPublishOptions = defaultRoomOptions.defaultAudioPublishOptions(),
        defaultDataPublishOptions = defaultRoomOptions.defaultDataPublishOptions(),
        adaptiveStream = false,
        dynacast = false,
        stopLocalTrackOnUnpublish = true,
        suspendLocalVideoTracksInBackground = false,
        e2eeOptions = null,
        reportRemoteTrackStatistics = false,
    )
    internal val room: Room = Room(null, connectOptions, roomOptions)
    private val participants = MutableStateFlow<List<InternalRemoteParticipant>>(emptyList())

    // Guards the read-check-create-emit on [participants]: connectWithUrl() fans
    // onParticipantConnected() out concurrently for every pre-existing participant.
    private val participantsMutex = Mutex()

    val remoteParticipants = participants.asStateFlow()
    val localParticipant = InternalLocalParticipant(scope, room.localParticipant()).also {
        // Register the LiveKit Text Stream chat handler now that we have a Room
        // handle — LocalParticipant.requireRoom() is internal on the Pod so we
        // pass the Room down explicitly here.
        it.registerChatTextStream(room)
    }

    @OptIn(ExperimentalForeignApi::class)
    suspend fun connectWithUrl(
        url: String,
        token: String,
        enableMicrophone: Boolean
    ) {
        suspendCoroutine { continuation ->
            room.connectWithUrl(
                url,
                token,
                null,
                null,
            ) { error ->
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }

        // Fallback: the primary trigger lives in the onConnectionState wrapper below,
        // fired by `roomDidConnect`. Re-emit here too in case the delegate callback
        // hasn't fired by the time the suspendCoroutine resumes.
        localParticipant.refreshNameFromNative()

        room.remoteParticipants().values.forEach { onParticipantConnected(it as RemoteParticipant) }

        if (enableMicrophone) {
            // A moderator may have revoked this participant's microphone publishing
            // (can_publish_sources). The native publish then fails — catch it so a
            // restricted participant still joins (muted; the UI greys the mic control)
            // instead of crashing connect. Re-throw cancellation so it isn't masked.
            try {
                localParticipant.enableMicrophone(true)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Log.d("RoomDelegate", "Microphone not enabled on connect: ${error.message}")
            }
        }
    }

    private val delegates: List<RoomDelegateProtocol> = listOf(
        delegateWrapper.wrapRoomDelegateWithDelegate(
            RoomDelegateConnectionState(
                onConnectionState = { state ->
                    // Re-read the native `name` on Connected BEFORE propagating the
                    // state so downstream observers (UI tiles) see the name in the
                    // same frame the connection becomes ready — avoids an
                    // "Anonymous" flash. Synchronous on purpose
                    // (refreshNameFromNative uses stateFlow.update).
                    if (state == ConnectionState.Connected) {
                        localParticipant.refreshNameFromNative()
                    }
                    // Purge the remote list on terminal states (didDisconnectWithError maps to
                    // either Disconnected or ConnectionError) so a rejoin reusing this Room
                    // starts clean instead of inheriting stale connected=false zombies.
                    if (state == ConnectionState.Disconnected || state is ConnectionState.ConnectionError) {
                        scope.launch { purgeParticipants() }
                    }
                    emit(state)
                },
                onParticipantConnected = { onParticipantConnected(it) },
                onParticipantDisconnected = { onParticipantDisconnected(it) },
                onParticipantAttributesUpdated = { participant, attributes ->
                    onAttributesUpdated(participant, attributes)
                },
                onIsRecordingUpdated = onIsRecordingUpdated
            )
        ),
    )

    private fun onAttributesUpdated(
        participant: LiveKitClient.Participant,
        attributes: Map<String, String>
    ) {
        val identity = participant.identity()?.stringValue() ?: return
        if (identity == localParticipant.identity) {
            localParticipant.onAttributesUpdatedFromRoom(attributes)
        } else {
            participants.value.find { it.identity == identity }
                ?.onAttributesUpdatedFromRoom(attributes)
        }
    }

    init {
        Log.d("RoomDelegate", "RoomDelegate initialization")
        delegates.forEach {
            Log.d("RoomDelegate", "adding $it to the room's delegate ($room")
            delegateWrapper.appendToRoom(room, it)
        }
    }

    fun disconnect() {
        room.disconnectWithCompletionHandler {
            // disconnected
        }
    }

    private fun onParticipantConnected(participant: RemoteParticipant) {
        scope.launch {
            val identity = participant.identity()?.stringValue()

            // Read-check-create-emit must be atomic as a unit. connectWithUrl()
            // fans this out concurrently for every pre-existing participant; a
            // plain `participants.value` read + `emit(list + new)` races — two
            // coroutines read the same list and the last emit wins, so a
            // participant is silently dropped from the grid (or, when the same
            // identity is processed twice, added in duplicate).
            participantsMutex.withLock {
                val list = participants.value
                val existing = if (identity != null) list.firstOrNull { it.identity == identity } else null
                when {
                    existing == null -> {
                        val newParticipant = InternalRemoteParticipant(scope, participant, true)
                        newParticipant.onConnect()
                        participants.emit(list + newParticipant)
                    }
                    // Same identity already present but disconnected — a stale entry left by a
                    // local leave/rejoin or a fast reconnect. Replace it with a fresh wrapper so
                    // the (re)joining participant becomes visible again instead of being skipped.
                    !existing.state.value.connected -> {
                        existing.onDisconnect()
                        val newParticipant = InternalRemoteParticipant(scope, participant, true)
                        newParticipant.onConnect()
                        participants.emit(list.filterNot { it === existing } + newParticipant)
                    }
                    // Same identity AND still connected -> genuine duplicate, ignore.
                    else -> Unit
                }
            }
        }
    }

    private fun onParticipantDisconnected(participant: RemoteParticipant) {
        // Read the identity SYNCHRONOUSLY on the delegate thread: by the time scope.launch runs,
        // iOS has already torn down the native participant and identity() returns null (Android
        // keeps it), so the entry would never match and never be removed — the participant lingers
        // and shows up duplicated when they rejoin with a fresh identity.
        val identity = participant.identity()?.stringValue()
        scope.launch {
            participantsMutex.withLock {
                // Match by identity only when non-null; remove by object reference so exactly
                // one entry drops (a null-identity event must not wipe all null-identity entries).
                val target = if (identity != null) {
                    participants.value.firstOrNull { it.identity == identity }
                } else {
                    null
                }
                target?.onDisconnect()
                if (target != null) {
                    participants.emit(participants.value.filterNot { it === target })
                }
            }
        }
    }

    // Drop every remote participant on terminal connection states. Emits connected=false
    // on each first so already-attached collectors clean their cells, not a silent wipe.
    private suspend fun purgeParticipants() {
        participantsMutex.withLock {
            val current = participants.value
            if (current.isEmpty()) return@withLock
            current.forEach { it.onDisconnect() }
            participants.emit(emptyList())
        }
    }
}
