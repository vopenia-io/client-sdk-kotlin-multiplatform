package io.vopenia.app.http

import kotlinx.serialization.Serializable

@Serializable
data class TokenAnswer(
    val token: String,
    val url: String
)
