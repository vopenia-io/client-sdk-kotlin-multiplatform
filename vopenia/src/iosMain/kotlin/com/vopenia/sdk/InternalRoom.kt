package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
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
}