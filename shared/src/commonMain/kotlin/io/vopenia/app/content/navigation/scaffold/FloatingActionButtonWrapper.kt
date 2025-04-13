package io.vopenia.app.content.navigation.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eu.codlab.compose.widgets.spacers.BottomSpacer
import io.vopenia.app.LocalApp

@Composable
fun FloatingActionButtonWrapper() {
    val state by LocalApp.current.states.collectAsState()

    Column {
        state.floatingActionButtonState?.let {
            FloatingActionButton(
                onClick = it.action
            ) {
                Icon(
                    imageVector = it.icon,
                    contentDescription = it.contentDescription
                )
            }

            BottomSpacer()
        }
    }
}
