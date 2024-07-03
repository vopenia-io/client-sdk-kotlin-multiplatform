package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

actual class Room actual constructor() {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val connectionStateEmitter: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Default)
    actual val connectionState: StateFlow<ConnectionState> = connectionStateEmitter.asStateFlow()

    private val roomDelegate = RoomDelegate()

    actual suspend fun connect(
        url: String,
        token: String,
    ) = roomDelegate.connectWithUrl(url, token)

    private fun emit(connectionState: ConnectionState) {
        scope.launch {
            connectionStateEmitter.emit(connectionState)
        }
    }
}