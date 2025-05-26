package com.vopenia.livekit

import com.vopenia.livekit.events.ConnectionState
import com.vopenia.livekit.participant.local.LocalParticipant
import com.vopenia.livekit.participant.remote.RemoteParticipant
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
            println("launching new event $it")
            connectionStateEmitter.emit(it)
        }
    }

    actual suspend fun connect(
        url: String,
        token: String,
    ) = roomDelegate.connectWithUrl(url, token)

    actual fun disconnect() {
        roomDelegate.disconnect()
    }

    actual val localParticipant: LocalParticipant
        get() = roomDelegate.localParticipant

    actual val remoteParticipants: StateFlow<List<RemoteParticipant>> =
        roomDelegate.remoteParticipants
}
