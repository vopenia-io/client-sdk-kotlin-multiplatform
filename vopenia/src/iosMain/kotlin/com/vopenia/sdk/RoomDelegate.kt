package com.vopenia.sdk

import LiveKitClient.ConnectOptions
import LiveKitClient.Room
import LiveKitClient.RoomOptions
import LiveKitClient.addDelegate
import com.vopenia.sdk.events.ConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class RoomDelegate(
    private val scope: CoroutineScope,
    private val emit: (ConnectionState) -> Unit
) {
    @OptIn(ExperimentalForeignApi::class)
    suspend fun connectWithUrl(
        url: String,
        token: String,
    ) {
        room.addDelegate(
            delegate
        )
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

    private val connectOptions = ConnectOptions()

    private val roomOptions = RoomOptions()

    val delegate = RoomDelegateProtocolOverride(emit)

    val room: Room = Room(delegate, connectOptions, roomOptions)

    fun disconnect() {
        room.disconnectWithCompletionHandler {
            emit(ConnectionState.Disconnected)
        }
    }
}