@file:OptIn(ExperimentalForeignApi::class)

package com.vopenia.sdk

import LiveKitClient.Room
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class Room actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    private val room: Room = Room()

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