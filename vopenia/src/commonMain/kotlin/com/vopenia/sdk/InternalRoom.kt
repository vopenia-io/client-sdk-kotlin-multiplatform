package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal expect class InternalRoom constructor(
    scope: CoroutineScope,
    connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    suspend fun connect(url: String, token: String)

    fun disconnect()
}