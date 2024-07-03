package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

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
}