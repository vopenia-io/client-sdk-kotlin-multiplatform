package com.vopenia.livekit

import com.vopenia.livekit.events.ConnectionState
import com.vopenia.livekit.permissions.Permission
import com.vopenia.livekit.permissions.PermissionRefused
import com.vopenia.livekit.permissions.PermissionsController
import com.vopenia.sdk.utils.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Room {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val connectionStateEmitter: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Default)
    val connectionState: StateFlow<ConnectionState> = connectionStateEmitter.asStateFlow()

    internal val internalRoom = InternalRoom(
        scope,
        connectionStateEmitter
    )

    val remoteParticipants = internalRoom.remoteParticipants

    val localParticipant = internalRoom.localParticipant

    suspend fun connect(
        url: String,
        token: String,
    ) {
        if (!PermissionsController.isGranted(Permission.RECORD_AUDIO)) {
            PermissionsController.providePermission(Permission.RECORD_AUDIO)
        }

        if (!PermissionsController.isGranted(Permission.RECORD_AUDIO)) {
            throw PermissionRefused(Permission.RECORD_AUDIO)
        }

        internalRoom.connect(url, token)
    }

    fun disconnect() {
        internalRoom.disconnect()
    }
}
