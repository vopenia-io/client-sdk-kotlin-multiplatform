package com.vopenia.sdk.events

sealed class ConnectionState {
    data object Default : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data object Disconnected : ConnectionState()
    class ConnectionError(val error: Throwable) : ConnectionState()
}
