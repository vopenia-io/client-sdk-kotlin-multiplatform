package io.vopenia.app.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.vopenia.app.LocalApp
import io.vopenia.app.content.initialize.InitializeScreen

@Composable
fun AppContent() {
    // nothing for now

    val model = LocalApp.current
    val state by model.states.collectAsState()

    when {
        !state.initialized -> InitializeScreen()
        else -> {

        }
    }
}