package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.InternalLocalParticipant
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("UnusedPrivateMember")
internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    actual suspend fun connect(url: String, token: String, enableMicrophone: Boolean) {
        // nothing for now
    }

    actual fun disconnect() {
        // nothing for now
    }

    actual val localParticipant: LocalParticipant
        get() = InternalLocalParticipant(scope)

    private val remoteParticipantsState = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    actual val remoteParticipants: StateFlow<List<RemoteParticipant>>
        get() = remoteParticipantsState.asStateFlow()
}
