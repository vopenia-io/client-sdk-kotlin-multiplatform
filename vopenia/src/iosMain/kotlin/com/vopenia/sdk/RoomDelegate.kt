package com.vopenia.sdk

import LiveKitClient.ConnectOptions
import LiveKitClient.Room
import LiveKitClient.RoomOptions
import LiveKitClient.addDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomDelegate {
    @OptIn(ExperimentalForeignApi::class)
    suspend fun connectWithUrl(
        url: String,
        token: String,
    ) {
        suspendCoroutine { continuation ->
            room.connectWithUrl(
                url,
                token,
                null,
                null,
            ) { error ->
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private val connectOptions = ConnectOptions()

    @OptIn(ExperimentalForeignApi::class)
    private val roomOptions = RoomOptions()

    /*@OptIn(ExperimentalForeignApi::class)
    val delegate: RoomDelegateProtocol = object : RoomDelegateProtocol {
        override fun roomDidConnect(room: Room) {
            super.roomDidConnect(room)
        }

        override fun roomDidReconnect(room: Room) {
            super.roomDidReconnect(room)
        }

        override fun roomIsReconnecting(room: Room) {
            super.roomIsReconnecting(room)
        }*/

    // also called when didFailedToConnectWithError
    /*override fun room(room: Room, didDisconnectWithError: LiveKitError?) {
        emit(
            ConnectionState.ConnectionError(
                NSErrorException(didDisconnectWithError!!)
            )
        )
    }*/
    //}

    @OptIn(ExperimentalForeignApi::class)
    val room: Room = Room(/*delegate, connectOptions, roomOptions*/)
}