package io.vopenia.app.content.navigation.scaffold

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import eu.codlab.compose.widgets.TextNormal
import io.vopenia.app.LocalApp
import io.vopenia.app.content.LocalMenuState
import io.vopenia.app.content.LocalNavigator
import io.vopenia.app.widgets.AppBarState
import io.vopenia.app.widgets.LocalWindow
import io.vopenia.app.widgets.TopAppBarExtended
import kotlinx.coroutines.launch

@Composable
fun TopBarWrapper() {
    val scaffold = LocalMenuState.current
    val appModel = LocalApp.current
    val navigator = LocalNavigator.current

    val scope = rememberCoroutineScope()

    val state by appModel.states.collectAsState()

    if(state.appBarState is AppBarState.Hidden) {
        return
    }

    val canGoBack by navigator.canGoBack.collectAsState(initial = false)

    val isScreenExpanded = LocalWindow.current.isScreenExpanded()

    Surface(elevation = 8.dp) {
        TopAppBarExtended(
            title = state.appBarState.showTitle(),
            topSpacer = true,
            canGoBack = canGoBack,
            isScreenExpanded = isScreenExpanded,
            appModel = appModel
        ) {
            if (canGoBack) {
                navigator.goBack()
                return@TopAppBarExtended
            }

            scope.launch {
                scaffold?.drawerState?.let {
                    if (it.isOpen) {
                        it.close()
                    } else {
                        it.open()
                    }
                }
            }
        }
    }
}
