package io.vopenia.app.http

import kotlinx.serialization.Serializable

@Serializable
data class TokenBody(
    val participant: String,
    val room: String
)