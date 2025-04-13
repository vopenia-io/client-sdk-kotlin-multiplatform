package io.vopenia.app.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.vopenia.app.content.navigation.scaffold.TopBarWrapper
import eu.codlab.compose.widgets.StatusBarAndNavigation
import eu.codlab.safearea.views.SafeArea
import eu.codlab.safearea.views.SafeAreaBehavior
import io.vopenia.app.LocalApp
import io.vopenia.app.content.navigation.NavigateTo
import io.vopenia.app.content.navigation.scaffold.FloatingActionButtonWrapper
import io.vopenia.app.content.navigation.scaffold.ScaffoldContentWrapper
import io.vopenia.app.content.pages.initialize.InitializeScreen
import io.vopenia.app.widgets.rememberSizeAwareScaffoldState
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator

val LocalMenuState: ProvidableCompositionLocal<ScaffoldState?> =
    compositionLocalOf { null }
val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    compositionLocalOf { error("No LocalNavigator defined") }

@Composable
fun AppContent() {
    StatusBarAndNavigation()
    val scaffoldState = rememberSizeAwareScaffoldState()
    val navigator = rememberNavigator("AppContent")

    val model = LocalApp.current

    LaunchedEffect(model, navigator, scaffoldState) {
        model.navigator = navigator
        model.scaffoldState = scaffoldState
    }

    val onMenuItemSelected: (String, NavigateTo) -> Unit = { newTitle, path ->
        model.show(path)
    }

    CompositionLocalProvider(
        LocalMenuState provides scaffoldState,
        LocalNavigator provides navigator
    ) {
        SafeArea(
            SafeAreaBehavior(
                extendToTop = true,
                extendToBottom = true,
                extendToStart = true,
                extendToEnd = true
            )
        ) {
            Column {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { TopBarWrapper() },
                    floatingActionButton = { FloatingActionButtonWrapper() },
                    content = {
                        ScaffoldContentWrapper(
                            onMenuItemSelected = onMenuItemSelected
                        )
                    }
                )
            }
        }
    }
}
