package io.vopenia.livekit

import io.vopenia.livekit.events.ConnectionState
import io.vopenia.livekit.permissions.Permission
import io.vopenia.livekit.permissions.PermissionRefused
import io.vopenia.livekit.permissions.PermissionsController
import io.vopenia.sdk.utils.Dispatchers
import io.vopenia.sdk.utils.Log
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

    init {
        Log.d("Room", "creating room for sdk v0.0.9-alpha4")
    }

    suspend fun connect(
        url: String,
        token: String,
        enableMicrophone: Boolean = true
    ) {
        if (!PermissionsController.isGranted(Permission.RECORD_AUDIO)) {
            PermissionsController.providePermission(Permission.RECORD_AUDIO)
        }

        if (!PermissionsController.isGranted(Permission.RECORD_AUDIO)) {
            throw PermissionRefused(Permission.RECORD_AUDIO)
        }

        internalRoom.connect(url, token, enableMicrophone)
    }

    fun disconnect() {
        internalRoom.disconnect()
    }
}
