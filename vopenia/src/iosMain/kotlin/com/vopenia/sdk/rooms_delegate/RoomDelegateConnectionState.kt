package com.vopenia.sdk.rooms_delegate

import LiveKitClient.ConnectionState
import LiveKitClient.LiveKitError
import LiveKitClient.RemoteParticipant
import LiveKitClient.Room
import LiveKitClient.RoomDelegateProtocol
import com.vopenia.sdk.NSErrorException
import com.vopenia.sdk.participant.InternalRemoteParticipant
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.darwin.NSObject
import com.vopenia.sdk.events.ConnectionState as CS

@OptIn(ExperimentalForeignApi::class)
class RoomDelegateConnectionState(
    private val onConnectionState: (CS) -> Unit,
    private val onParticipantConnected: (RemoteParticipant) -> Unit,
    private val onParticipantDisconnected: (RemoteParticipant) -> Unit,
) : RoomDelegateProtocol, NSObject() {
    override fun roomDidConnect(room: Room) {
        println("roomDidConnect")
        onConnectionState(CS.Connected)
    }

    override fun roomDidReconnect(room: Room) {
        println("roomDidReconnect")
        onConnectionState(CS.Connected)
    }

    override fun room(
        room: Room,
        didUpdateConnectionState: ConnectionState,
        from: ConnectionState
    ) {
        println("new state $didUpdateConnectionState")
        when (didUpdateConnectionState) {
            LiveKitClient.ConnectionStateConnected -> onConnectionState(CS.Connected)
            LiveKitClient.ConnectionStateConnecting -> onConnectionState(CS.Connecting)
            LiveKitClient.ConnectionStateDisconnected -> onConnectionState(CS.Disconnected)
            LiveKitClient.ConnectionStateReconnecting -> onConnectionState(CS.Connecting)
            else -> {
                // nothing
            }
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun room(room: Room, didDisconnectWithError: LiveKitError?) {
        onConnectionState(CS.ConnectionError(NSErrorException(didDisconnectWithError!!)))
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun room(room: Room, didFailToConnectWithError: LiveKitError?) {
        onConnectionState(CS.ConnectionError(NSErrorException(didFailToConnectWithError!!)))
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun room(room: Room, participantDidConnect: RemoteParticipant) {
        onParticipantConnected(participantDidConnect)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun room(room: Room, participantDidDisconnect: RemoteParticipant) {
        onParticipantConnected(participantDidDisconnect)
    }
}