package io.vopenia.app

import androidx.compose.foundation.isSystemInDarkTheme
import eu.codlab.safearea.views.WindowInsetsUIViewController
import moe.tlaster.precompose.PreComposeApp

@Suppress("FunctionNaming")
fun MainViewController() = WindowInsetsUIViewController {
    val isSystemDarkTheme = isSystemInDarkTheme()

    PreComposeApp {
        App(
            isDarkTheme = isSystemDarkTheme
        )
    }
}
