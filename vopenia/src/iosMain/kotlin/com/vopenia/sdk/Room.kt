@file:OptIn(ExperimentalForeignApi::class)

package com.vopenia.sdk

import LiveKitClient.Room
import LiveKitClient.RoomDelegateProtocol
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
actual class Room actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    private val room: Room = Room()

    private val delegate: RoomDelegateProtocol = object: RoomDelegateProtocol {
        override fun roomDidConnect(room: Room) {
            super.roomDidConnect(room)
        }

        override fun roomDidReconnect(room: Room) {
            super.roomDidReconnect(room)
        }

        override fun roomIsReconnecting(room: Room) {
            super.roomIsReconnecting(room)
        }
    }

    actual suspend fun connect(
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
}