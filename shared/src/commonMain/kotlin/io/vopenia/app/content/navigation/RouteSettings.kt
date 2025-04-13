package io.vopenia.app.content.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.vopenia.app.AppModel
import io.vopenia.app.LocalApp
import io.vopenia.app.content.pages.settings.SettingsScreen
import io.vopenia.app.widgets.AppBarState
import io.vopenia.app.widgets.MenuItem
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

class RouteSettings : Route(
    "/settings",
    navTransition = NavTransition(),
    swipeProperties = SwipeProperties()
) {
    @Composable
    override fun scene(backStackEntry: BackStackEntry) {
        SettingsScreen(Modifier.fillMaxSize())
    }

    override fun onEntryIsActive(
        appModel: AppModel,
        defaultActions: List<MenuItem>,
        backStackEntry: BackStackEntry
    ) {
        appModel.setAppBarState(AppBarState.Hidden)
    }
}
