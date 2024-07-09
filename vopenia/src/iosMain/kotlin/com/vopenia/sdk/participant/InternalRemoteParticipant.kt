package com.vopenia.sdk.participant

import LiveKitClient.addDelegate
import LiveKitClient.removeDelegate
import com.vopenia.sdk.participant.delegate.LocalParticipantDelegate
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipantState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import LiveKitClient.RemoteParticipant as RP

@OptIn(ExperimentalForeignApi::class)
class InternalRemoteParticipant(
    private val scope: CoroutineScope,
    private val remoteParticipant: RP,
    connected: Boolean
) : RemoteParticipant {
    private var isAttached = false

    private val stateFlow: MutableStateFlow<RemoteParticipantState> = MutableStateFlow(
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
            },
            onTrackUnpublished = { track ->
                println("onTrackUnpublished $track")
            },
            onTrackPublicationIsMuted = { track, isMuted ->
                println("onTrackPublicationIsMuted $track")
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

}