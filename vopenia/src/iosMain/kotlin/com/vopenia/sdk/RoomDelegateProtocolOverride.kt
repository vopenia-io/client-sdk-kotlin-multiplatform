package com.vopenia.sdk

import LiveKitClient.ConnectionState
import LiveKitClient.LiveKitError
import LiveKitClient.Room
import LiveKitClient.RoomDelegateProtocol
import com.vopenia.sdk.events.ConnectionState as CS
import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class RoomDelegateProtocolOverride(
    private val onConnectionState: (CS) -> Unit
) : NSObject(), RoomDelegateProtocol {
    override fun room(room: Room, didFailToConnectWithError: LiveKitError?) {
        onConnectionState(CS.ConnectionError(NSErrorException(didFailToConnectWithError!!)))
    }

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
}