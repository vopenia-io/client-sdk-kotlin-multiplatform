package io.vopenia.app.content.navigation

import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.PopUpTo

sealed class NavigateTo {
    abstract val route: String
    abstract val popBackStack: Boolean
    abstract val options: NavOptions

    data object Initialize : NavigateTo() {
        override val route = "/initialize"
        override val popBackStack = true

        override val options = NavOptions(
            launchSingleTop = false,
            popUpTo = PopUpTo.First(true)
        )
    }

    class Main : NavigateTo() {
        override val route = "/main"
        override val popBackStack = true

        override val options = NavOptions(
            launchSingleTop = false,
            popUpTo = PopUpTo.First(true)
        )
    }

    class Settings() : NavigateTo() {
        override val route = "/settings"
        override val popBackStack = true

        override val options = NavOptions(
            launchSingleTop = false,
            popUpTo = PopUpTo.First(true)
        )
    }
}
