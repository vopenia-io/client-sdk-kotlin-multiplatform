package io.vopenia.app.content.initialize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import eu.codlab.compose.widgets.StatusBarAndNavigation
import io.vopenia.app.LocalApp
import io.vopenia.app.PreviewApp
import io.vopenia.app.theme.AppColor

@Composable
fun InitializeScreen(
    modifier: Modifier = Modifier
) {
    val globalApp = LocalApp.current

    LaunchedEffect(Unit) {
        if (!globalApp.isInitialized()) {
            globalApp.initialize()
        }
    }

    StatusBarAndNavigation()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColor.BackgroundBlue)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .heightIn(0.dp, 400.dp) // mention max height here
                .widthIn(0.dp, 800.dp) // mention max width here
        ) {
            CircularProgressIndicator()
        }
    }
}

@HotPreview(widthDp = 500, heightDp = 400, darkMode = true)
@HotPreview(widthDp = 500, heightDp = 400, darkMode = false)
@Composable
fun InitializeScreenPreview() {
    PreviewApp(modifier = Modifier.fillMaxSize()) {
        InitializeScreen(modifier = Modifier.fillMaxSize())
    }
}
