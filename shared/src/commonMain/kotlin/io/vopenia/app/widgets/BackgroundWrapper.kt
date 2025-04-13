package io.vopenia.app.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BackgroundWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        BackgroundImage()

        content()

        DrawOverlay()
    }
}

@Composable
private fun BoxScope.BackgroundImage() {
    /*val painter = if (LocalDarkTheme.current) {
        painterResource(Res.drawable.bg_dark)
    } else {
        painterResource(Res.drawable.bg_light)
    }

    Image(
        modifier = Modifier.fillMaxSize().align(Alignment.Center),
        painter = painter,
        contentDescription = "",
        contentScale = ContentScale.Crop
    )*/
}

@Composable
private fun BoxScope.DrawOverlay() {
    // nothing for now but could be something like angles etc...
}
