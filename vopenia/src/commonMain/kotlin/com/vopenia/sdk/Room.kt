package com.vopenia.sdk

expect class Room constructor() {
    suspend fun connect(url: String, token: String)
}