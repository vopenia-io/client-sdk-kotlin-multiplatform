package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.participant.remote.RemoteParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal expect class InternalRoom constructor(
    scope: CoroutineScope,
    connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    val localParticipant: LocalParticipant

    val remoteParticipants: StateFlow<List<RemoteParticipant>>

    suspend fun connect(url: String, token: String)

    fun disconnect()
}