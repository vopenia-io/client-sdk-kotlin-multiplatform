package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import com.vopenia.sdk.participant.local.LocalParticipant
import com.vopenia.sdk.permissions.Permission
import com.vopenia.sdk.permissions.PermissionRefused
import com.vopenia.sdk.permissions.PermissionsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class Room {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val connectionStateEmitter: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.Default)
    val connectionState: StateFlow<ConnectionState> = connectionStateEmitter.asStateFlow()

    private val internalRoom = InternalRoom(
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
}