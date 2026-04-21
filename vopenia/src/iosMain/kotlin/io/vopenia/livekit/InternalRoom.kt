package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.participant.local.LocalParticipant
import io.vopenia.livekit.participant.remote.RemoteParticipant
import io.vopenia.sdk.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal actual class InternalRoom actual constructor(
    private val scope: CoroutineScope,
    private val connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    private val roomDelegate = RoomDelegate(scope) {
        scope.launch {
            Log.d("InternalRoom", "launching new event $it")
            connectionStateEmitter.emit(it)
        }
    }

    actual suspend fun connect(
        url: String,
        token: String,
        enableMicrophone: Boolean
    ) {
        // first we reset the connection state
        connectionStateEmitter.emit(ConnectionState.Connecting)

        roomDelegate.connectWithUrl(url, token, enableMicrophone)
    }

    actual fun disconnect() {
        roomDelegate.disconnect()
    }

    actual val localParticipant: LocalParticipant
        get() = roomDelegate.localParticipant

    actual val remoteParticipants: StateFlow<List<RemoteParticipant>> =
        roomDelegate.remoteParticipants
}
