package io.vopenia.app.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.vopenia.app.LocalApp
import io.vopenia.app.content.initialize.InitializeScreen
import io.vopenia.app.content.join.JoinScreen

@Composable
fun AppContent() {
    // nothing for now

    val model = LocalApp.current
    val state by model.states.collectAsState()
    val modifier = Modifier.fillMaxSize()

    when {
        !state.initialized -> InitializeScreen(modifier)
        null == state.room -> JoinScreen(modifier)
        else -> {

        }
    }
}