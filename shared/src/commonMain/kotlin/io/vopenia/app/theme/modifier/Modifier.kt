package io.vopenia.app.theme.modifier

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import eu.codlab.compose.theme.LocalDarkTheme
import io.vopenia.app.theme.AppColor

fun Modifier.defaultBackground() = composed {
    this.background(
        if (LocalDarkTheme.current) {
            AppColor.BackgroundBlue
        } else {
            AppColor.GrayExtraLight // gray hyper light
        }
    )
}
