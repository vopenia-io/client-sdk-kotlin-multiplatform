package com.vopenia.livekit

import com.vopenia.livekit.events.ConnectionState
import com.vopenia.livekit.participant.local.LocalParticipant
import com.vopenia.livekit.participant.remote.RemoteParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal expect class InternalRoom(
    scope: CoroutineScope,
    connectionStateEmitter: MutableStateFlow<ConnectionState>
) {
    val localParticipant: LocalParticipant

    val remoteParticipants: StateFlow<List<RemoteParticipant>>

    suspend fun connect(url: String, token: String)

    fun disconnect()
}
