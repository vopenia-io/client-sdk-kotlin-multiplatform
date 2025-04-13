package io.vopenia.app.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.codlab.compose.theme.LocalDarkTheme
import eu.codlab.compose.widgets.spacers.TopSpacer
import io.vopenia.app.AppModel
import io.vopenia.app.LocalFontSizes
import io.vopenia.app.theme.AppColor
import io.vopenia.app.utils.localized
import org.jetbrains.compose.resources.StringResource

@Composable
fun TopAppBarExtended(
    title: String,
    topSpacer: Boolean,
    canGoBack: Boolean,
    isScreenExpanded: Boolean, // unused for now in the app
    appModel: AppModel,
    onNavigationClick: () -> Unit
) {
    val modelState by appModel.states.collectAsState()
    val isDarkTheme = LocalDarkTheme.current

    val appBarState = modelState.appBarState

    val menu = if (canGoBack) {
        Icons.AutoMirrored.Filled.ArrowBack
        // } else if (!isScreenExpanded) {
        //    Icons.Filled.Menu
    } else {
        null
    }

    val tint = if (isDarkTheme) AppColor.White else AppColor.Black

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (topSpacer) {
            TopSpacer(
                if (isDarkTheme) {
                    Color.Black
                } else {
                    Color.White
                }
            )
        }

        TopAppBar(
            elevation = 4.dp,
            title = {
                Text(
                    fontSize = LocalFontSizes.current.actionBar.title,
                    text = title,
                    color = tint
                )
            },
            backgroundColor = if (isDarkTheme) AppColor.Black else AppColor.White,
            navigationIcon = if (null != menu) {
                {
                    IconButton(
                        onClick = onNavigationClick
                    ) {
                        Icon(
                            menu,
                            null,
                            tint = tint
                        )
                    }
                }
            } else {
                null
            },
            actions = {
                appBarState.actions?.forEach {
                    it.Draw(this, tint)
                }
            }
        )
    }
}

sealed class AppBarState(
    val actions: List<MenuItem>? = null
) {
    @Composable
    abstract fun showTitle(): String

    object Hidden : AppBarState() {
        @Composable
        override fun showTitle() = "hidden"
    }

    class Regular(
        val title: String = "",
        actions: List<MenuItem>? = null
    ) : AppBarState(actions) {
        @Composable
        override fun showTitle() = title
    }

    class Localized(
        val title: StringResource,
        actions: List<MenuItem>? = null
    ) : AppBarState(actions) {
        @Composable
        override fun showTitle() = title.localized()
    }
}

data class FloatingActionButtonState(
    val icon: ImageVector,
    val contentDescription: String,
    val action: () -> Unit
)
