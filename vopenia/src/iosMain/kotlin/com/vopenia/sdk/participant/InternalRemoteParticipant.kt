package com.vopenia.sdk.participant

import LiveKitClient.RemoteTrackPublication
import LiveKitClient.addDelegate
import LiveKitClient.removeDelegate
import com.vopenia.sdk.participant.delegate.LocalParticipantDelegate
import com.vopenia.sdk.participant.delegate.RemoteParticipantDelegate
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import com.vopenia.sdk.participant.track.InternalRemoteTrack
import com.vopenia.sdk.participant.track.RemoteTrack
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import LiveKitClient.RemoteParticipant as RP

@OptIn(ExperimentalForeignApi::class)
class InternalRemoteParticipant(
    private val scope: CoroutineScope,
    private val remoteParticipant: RP,
    connected: Boolean
) : RemoteParticipant {
    private var isAttached = false

    private val remoteTracks = MutableStateFlow<List<InternalRemoteTrack>>(emptyList())
    override val tracks: StateFlow<List<RemoteTrack>> = remoteTracks.asStateFlow()

    private val stateFlow = MutableStateFlow(
        RemoteParticipantState(
            connected = connected,
            name = remoteParticipant.name(),
            metadata = remoteParticipant.metadata(),
            permissions = InternalParticipantPermissions(
                remoteParticipant.permissions()
            ).toMultiplatform()
        )
    )
    private val isSpeakingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val identity = remoteParticipant.identity()?.stringValue()

    override fun equals(other: Any?): Boolean {
        if (other is InternalRemoteParticipant) {
            return other.identity == identity
        }

        return false
    }

    private val delegate = remoteParticipant.wrapDelegateWithDelegate(
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
            }
        )
    )

    internal fun onConnect() {
        if (!isAttached) {
            remoteParticipant.addDelegate(delegate)
            isAttached = true

            scope.async {
                stateFlow.emit(stateFlow.value.copy(connected = true))
            }
        }
    }

    internal fun onDisconnect() {
        if (isAttached) {
            remoteParticipant.removeDelegate(delegate)
            isAttached = false

            scope.async {
                stateFlow.emit(stateFlow.value.copy(connected = false))
            }
        }
    }

    override val state: StateFlow<RemoteParticipantState>
        get() = stateFlow.asStateFlow()

    override val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    private fun append(track: InternalRemoteTrack) {
        scope.launch {
            remoteTracks.emit( remoteTracks.value + track)
        }
    }

    private fun getOrCreate(track: RemoteTrackPublication): Pair<InternalRemoteTrack, Boolean> =
        remoteTracks.value.find { it.sid == track.sid().stringValue() }.let {
            if (null != it) {
                it to false
            } else {
                InternalRemoteTrack(scope, track) to true
            }
        }

}