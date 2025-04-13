package io.vopenia.app.content.navigation.scaffold

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import io.vopenia.app.content.navigation.NavigateTo
import eu.codlab.compose.widgets.spacers.BottomSpacer
import eu.codlab.platform.Platform
import eu.codlab.platform.currentPlatform
import io.vopenia.app.LocalApp
import io.vopenia.app.content.LocalNavigator
import io.vopenia.app.content.navigation.PossibleRoutes
import io.vopenia.app.theme.WindowType
import io.vopenia.app.theme.modifier.defaultBackground
import io.vopenia.app.widgets.BackgroundWrapper
import io.vopenia.app.widgets.LocalWindow
import io.vopenia.app.window.LocalFrameProvider
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.transition.NavTransition

@Composable
fun ColumnScope.ScaffoldContentWrapper(
    onMenuItemSelected: (title: String, navigateTo: NavigateTo) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val navigator = LocalNavigator.current

    var tinyMenuInPhablet = LocalWindow.current == WindowType.PHABLET

    val interactionSource = remember { MutableInteractionSource() }
    val menuSizeForPhablet = if (tinyMenuInPhablet) {
        42.dp
    } else {
        250.dp
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (LocalWindow.current.isScreenExpanded()) {
            // drawer content here ?
        }

        LocalFrameProvider(
            modifier = Modifier.fillMaxSize()
        ) {
            BackgroundWrapper(
                modifier = Modifier.fillMaxSize().clickable(
                    interactionSource = interactionSource,
                    indication = null // this gets rid of the ripple effect
                ) {
                    if (currentPlatform != Platform.JVM) {
                        keyboardController?.hide()
                        focusManager.clearFocus(true)
                    }
                }
            ) {
                Column(modifier = Modifier.weight(1f).defaultBackground()) {
                    NavHost(
                        modifier = Modifier.weight(1f).defaultBackground(),
                        // Assign the navigator to the NavHost
                        navigator = navigator,
                        // Navigation transition for the scenes in this NavHost, this is optional
                        navTransition = NavTransition(),
                        // The start destination
                        initialRoute = NavigateTo.Initialize.route,
                        /*swipeProperties = SwipeProperties(
                        //spaceToSwipe = 50.dp
                    )*/
                    ) {
                        PossibleRoutes.entries.forEach {
                            println("Settings routes ${it.impl.route}")
                            scene(
                                route = it.impl.route,
                                navTransition = it.impl.navTransition,
                                swipeProperties = it.impl.swipeProperties
                            ) { backStackEntry ->
                                // TODO manage emptyList with "defaults" list
                                it.impl.onEntryIsActive(
                                    LocalApp.current,
                                    emptyList(),
                                    backStackEntry
                                )

                                it.impl.scene(backStackEntry)
                            }
                        }
                    }

                    BottomSpacer()
                }
            }
        }
    }
}
