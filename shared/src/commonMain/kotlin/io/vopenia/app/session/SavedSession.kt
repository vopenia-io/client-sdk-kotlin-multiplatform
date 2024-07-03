package io.vopenia.app.session

import kotlinx.serialization.Serializable

@Serializable
data class SavedSession(
    val userName: String
)
