package io.vopenia.app.android

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import eu.codlab.fleet.permissions.PermissionsController
import io.vopenia.app.App
import io.vopenia.app.AppBackPressProvider
import moe.tlaster.precompose.lifecycle.setContent

class MainActivity : FragmentActivity() {
    private val onBackPressProvider = AppBackPressProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionsController.setActivity(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Box {
                App(
                    isDarkTheme = isSystemInDarkTheme(),
                    onBackPressed = onBackPressProvider,
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        println("deprecated but called")
        if (!onBackPressProvider.onBackPress()) {
            super.onBackPressed()
        }
    }
}
