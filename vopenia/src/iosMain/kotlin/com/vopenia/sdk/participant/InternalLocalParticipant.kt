package com.vopenia.sdk.participant

import LiveKitClient.addDelegate
import LiveKitClient.setMicrophoneWithEnabled
import com.vopenia.sdk.NSErrorException
import com.vopenia.sdk.participant.delegate.LocalParticipantDelegate
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.local.LocalParticipantState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import LiveKitClient.LocalParticipant as LP

@OptIn(ExperimentalForeignApi::class)
class InternalLocalParticipant(
    private val scope: CoroutineScope,
    private val localParticipant: LP
) : LocalParticipant {
    private val stateFlow: MutableStateFlow<LocalParticipantState> = MutableStateFlow(
        LocalParticipantState(
            permissions = InternalParticipantPermissions(
                localParticipant.permissions()
            ).toMultiplatform()
        )
    )

    override val identity = localParticipant.identity()?.stringValue()

    private val isSpeakingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val delegate = localParticipant.wrapDelegateWithDelegate(
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

    init {
        localParticipant.addDelegate(delegate)
    }

    override val state: StateFlow<LocalParticipantState>
        get() = stateFlow.asStateFlow()

    override val isSpeakingState: StateFlow<Boolean>
        get() = isSpeakingFlow.asStateFlow()

    override suspend fun enableMicrophone(enabled: Boolean) {
        suspendCoroutine { continuation ->
            localParticipant.setMicrophoneWithEnabled(enabled, null, null) { track, error ->
                // todo manage here
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }
}
