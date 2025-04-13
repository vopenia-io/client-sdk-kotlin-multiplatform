package io.vopenia.app.content.navigation

import androidx.compose.runtime.Composable
import io.vopenia.app.AppModel
import io.vopenia.app.widgets.MenuItem
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

sealed class Route(
    val route: String,
    val deepLinks: List<String> = emptyList(),
    val navTransition: NavTransition? = null,
    val swipeProperties: SwipeProperties? = null,
) {
    @Composable
    abstract fun scene(
        backStackEntry: BackStackEntry
    )

    abstract fun onEntryIsActive(
        appModel: AppModel,
        defaultActions: List<MenuItem>,
        backStackEntry: BackStackEntry
    )
}
