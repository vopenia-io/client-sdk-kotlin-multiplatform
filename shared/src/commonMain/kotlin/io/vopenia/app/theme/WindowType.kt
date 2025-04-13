package io.vopenia.app.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowType {
    SMARTPHONE_TINY,
    SMARTPHONE,
    PHABLET,
    TABLET;

    fun isScreenExpanded() = when (this) {
        SMARTPHONE_TINY -> false
        SMARTPHONE -> false
        PHABLET -> true
        TABLET -> true
    }

    // Factory method that creates an instance of the class based on window width
    companion object {
        fun basedOnWidth(windowWidth: Dp): WindowType {
            return when {
                windowWidth < 250.dp -> SMARTPHONE_TINY
                windowWidth < 400.dp -> SMARTPHONE
                windowWidth < 700.dp -> PHABLET
                else -> TABLET
            }
        }
    }
}
