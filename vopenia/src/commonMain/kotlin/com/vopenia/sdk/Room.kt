package com.vopenia.sdk

import com.vopenia.sdk.events.ConnectionState
import kotlinx.coroutines.flow.StateFlow

expect class Room constructor() {
    val connectionState: StateFlow<ConnectionState>

    suspend fun connect(url: String, token: String)
}