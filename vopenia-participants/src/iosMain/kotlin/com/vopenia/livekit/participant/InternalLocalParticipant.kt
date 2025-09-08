package com.vopenia.livekit.participant

import LiveKitClient.setCameraWithEnabled
import LiveKitClient.setMicrophoneWithEnabled
import LiveKitClientKotlin.DelegateKotlin
import com.vopenia.livekit.NSErrorException
import com.vopenia.livekit.participant.delegate.LocalParticipantDelegate
import com.vopenia.livekit.participant.local.LocalParticipant
import com.vopenia.livekit.participant.local.LocalParticipantState
import com.vopenia.livekit.participant.track.Kind
import com.vopenia.livekit.participant.track.kindFrom
import com.vopenia.livekit.participant.track.local.LocalAudioTrack
import com.vopenia.livekit.participant.track.local.LocalNoneTrack
import com.vopenia.livekit.participant.track.local.LocalTrack
import com.vopenia.livekit.participant.track.local.LocalTrackPublication
import com.vopenia.livekit.participant.track.local.LocalVideoTrack
import com.vopenia.livekit.participant.transcription.TranscriptionSegment
import com.vopenia.livekit.permissions.Permission
import com.vopenia.livekit.permissions.PermissionsController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
            permissions = InternalParticipantPermissions(
                localParticipant.permissions()
            ).toMultiplatform()
        )
    )

    override val transcriptsFlow = MutableSharedFlow<TranscriptionSegment>()

    override val identity = localParticipant.identity()?.stringValue()

    private val delegate = delegateWrapper.wrapParticipantDelegateWithDelegate(
        LocalParticipantDelegate(
            onConnectionQuality = { connectionQuality ->
                // scope.async {
                //
                // }
            },
            onIsSpeaking = { isSpeaking ->
                println("isSpeaking $isSpeaking")
                scope.async {
                    isSpeakingFlow.emit(isSpeaking)
                }
            },
            onMetadataUpdated = { metadata ->
                println("metadata $metadata")
                scope.async {
                    stateFlow.emit(stateFlow.value.copy(metadata = metadata))
                }
            },
            onNameUpdated = { name ->
                println("name $name")
                scope.async {
                    stateFlow.emit(stateFlow.value.copy(name = name))
                }
            },
            onPermissionsUpdated = { permissions ->
                println("permissions $permissions")
                scope.async {
                    stateFlow.emit(
                        stateFlow.value.copy(
                            permissions = InternalParticipantPermissions(
                                permissions
                            ).toMultiplatform()
                        )
                    )
                }
            },
            onTrackPublished = { track ->
                println("onTrackPublished $track")
                val (wrapper, new) = getOrCreate(track as LocalTrackPublication)

                wrapper.setPublished(true)
                if (new) append(wrapper)
            },
            onTrackUnpublished = { track ->
                println("onTrackUnpublished $track")
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
            }
        )
    )

    init {
        delegateWrapper.appendToParticipant(localParticipant, delegate)
    }

    override fun filterListAudio(tracks: List<LocalTrack>): List<LocalAudioTrack> {
        return tracks.filterIsInstance<LocalAudioTrack>()
    }

    override fun filterListVideo(tracks: List<LocalTrack>): List<LocalVideoTrack> {
        return tracks.filterIsInstance<LocalVideoTrack>()
    }

    override suspend fun enableMicrophone(enabled: Boolean) {
        PermissionsController.checkOrProvide(Permission.RECORD_AUDIO)
        println("enableMicrophone $enabled")
        suspendCoroutine { continuation ->
            localParticipant.setMicrophoneWithEnabled(enabled, null, null) { _, error ->
                // todo manage here
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    override suspend fun enableCamera(enabled: Boolean) {
        PermissionsController.checkOrProvide(Permission.CAMERA)

        suspendCoroutine { continuation ->
            localParticipant.setCameraWithEnabled(enabled, null, null) { _, error ->
                // todo manage here
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private fun getOrCreate(
        track: LocalTrackPublication
    ): Pair<LocalTrack, Boolean> {
        return internalTracks.value.find { it.sid == track.sid().stringValue() }.let {
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
}
