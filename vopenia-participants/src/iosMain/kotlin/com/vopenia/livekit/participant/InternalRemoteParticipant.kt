package com.vopenia.livekit.participant

import LiveKitClient.removeDelegate
import LiveKitClientKotlin.DelegateKotlin
import com.vopenia.livekit.participant.delegate.RemoteParticipantDelegate
import com.vopenia.livekit.participant.remote.RemoteParticipant
import com.vopenia.livekit.participant.remote.RemoteParticipantState
import com.vopenia.livekit.participant.track.Kind
import com.vopenia.livekit.participant.track.RemoteAudioTrack
import com.vopenia.livekit.participant.track.RemoteNoneTrack
import com.vopenia.livekit.participant.track.RemoteTrack
import com.vopenia.livekit.participant.track.RemoteTrackPublication
import com.vopenia.livekit.participant.track.RemoteVideoTrack
import com.vopenia.livekit.participant.track.StreamState
import com.vopenia.livekit.participant.track.kindFrom
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import LiveKitClient.RemoteParticipant as RP

@OptIn(ExperimentalForeignApi::class)
class InternalRemoteParticipant(
    scope: CoroutineScope,
    private val remoteParticipant: RP,
    connected: Boolean
) : RemoteParticipant(
    scope,
    RemoteParticipantState(
        connected = connected,
        name = remoteParticipant.name(),
        metadata = remoteParticipant.metadata(),
        permissions = InternalParticipantPermissions(
            remoteParticipant.permissions()
        ).toMultiplatform()
    )
) {
    private val delegateWrapper = DelegateKotlin()
    private var isAttached = false

    override fun filterListAudio(tracks: List<RemoteTrack>): List<RemoteAudioTrack> {
        return tracks.filterIsInstance<RemoteAudioTrack>()
    }

    override fun filterListVideo(tracks: List<RemoteTrack>): List<RemoteVideoTrack> {
        return tracks.filterIsInstance<RemoteVideoTrack>()
    }

    override val identity = remoteParticipant.identity()?.stringValue()

    private val delegate = delegateWrapper.wrapParticipantDelegateWithDelegate(
        RemoteParticipantDelegate(
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
                val (wrapper, new) = getOrCreate(track)

                wrapper.setPublished(true)
                if (new) append(wrapper)
            },
            onTrackUnpublished = { track ->
                val (wrapper, new) = getOrCreate(track)

                wrapper.setPublished(false)
                if (new) append(wrapper)
            },
            onTrackPublicationIsMuted = { track, isMuted ->
                println("onTrackPublicationIsMuted $track")
                val (wrapper, new) = getOrCreate(track as RemoteTrackPublication)

                wrapper.setMuted(isMuted)
                if (new) append(wrapper)
            },
            onTrackSubscribed = { track ->
                val (wrapper, new) = getOrCreate(track)

                wrapper.setSubscribed(true)
                if (new) append(wrapper)
            },
            onTrackUnsubscribed = { track ->
                val (wrapper, new) = getOrCreate(track)

                wrapper.setSubscribed(false)
                if (new) append(wrapper)
            },
            onTrackStreamStateChanged = { trackPublication, streamState ->
                val (wrapper, new) = getOrCreate(trackPublication)

                wrapper.setActive(streamState == StreamState.Active)
                if (new) append(wrapper)
            }
        )
    )

    fun onConnect() {
        if (isAttached) return

        remoteParticipant.trackPublications().values.forEach {
            if (it is RemoteTrackPublication) {
                val (wrapper, new) = getOrCreate(it)

                wrapper.setPublished(true)
                if (new) append(wrapper)
            }
        }

        println("added the delegate to the remote participant")
        delegateWrapper.appendToParticipant(remoteParticipant, delegate)
        isAttached = true

        scope.async {
            stateFlow.emit(stateFlow.value.copy(connected = true))
        }
    }

    fun onDisconnect() {
        if (!isAttached) return

        remoteParticipant.removeDelegate(delegate)
        isAttached = false

        scope.async {
            stateFlow.emit(stateFlow.value.copy(connected = false))
        }
    }

    private fun getOrCreate(track: RemoteTrackPublication): Pair<RemoteTrack, Boolean> =
        internalTracks.value.find { it.sid == track.sid().stringValue() }.let {
            if (null != it) {
                it to false
            } else {
                when (kindFrom(track.kind())) {
                    Kind.Audio -> RemoteAudioTrack(scope, track)
                    Kind.Video -> RemoteVideoTrack(scope, track)
                    Kind.None -> RemoteNoneTrack(scope, track)
                } to true
            }
        }
}
