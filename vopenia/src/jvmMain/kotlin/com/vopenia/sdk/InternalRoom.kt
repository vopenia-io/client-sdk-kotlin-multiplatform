package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import com.vopenia.sdk.participant.ParticipantPermissions
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.local.LocalParticipantState
import com.vopenia.sdk.participant.remote.RemoteParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    actual suspend fun connect(url: String, token: String) {
        // nothing for now
    }

    actual fun disconnect() {
        // nothing for now
    }

    actual val localParticipant: LocalParticipant
        get() = object : LocalParticipant {
            private val stateFlow = MutableStateFlow<LocalParticipantState>(
                LocalParticipantState(
                    permissions = ParticipantPermissions()
                )
            )

            private val isSpeakingStateFlow = MutableStateFlow(true)
            override val identity: String?
                get() = "Not yet implemented"

            override val state: StateFlow<LocalParticipantState>
                get() = stateFlow.asStateFlow()
            override val isSpeakingState: StateFlow<Boolean>
                get() = isSpeakingStateFlow.asStateFlow()

            override suspend fun enableMicrophone(enabled: Boolean) {
                // not available
            }

        }

    private val remoteParticipantsState = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    actual val remoteParticipants: StateFlow<List<RemoteParticipant>>
        get() = remoteParticipantsState.asStateFlow()
}