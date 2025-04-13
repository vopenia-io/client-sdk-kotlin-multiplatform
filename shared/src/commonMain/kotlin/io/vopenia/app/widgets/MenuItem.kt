package io.vopenia.app.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.codlab.compose.theme.LocalDarkTheme

sealed class MenuItem {
    @Composable
    abstract fun Draw(scope: RowScope, tint: Color)

    data class MenuItemSimple(
        private val imageVector: ImageVector,
        private val contentDescription: String = "",
        private val onClick: () -> Unit
    ) : MenuItem() {
        @Composable
        override fun Draw(scope: RowScope, tint: Color) {
            IconButton(onClick = onClick) {
                Icon(
                    tint = tint,
                    imageVector = imageVector,
                    contentDescription = contentDescription
                )
            }
        }
    }

    class MenuItemOverflowMenu(
        private val imageVector: ImageVector,
        private val contentDescription: String = "",
        private val content: @Composable RowScope.(
            dismiss: () -> Unit
        ) -> Unit
    ) : MenuItem() {
        @Composable
        override fun Draw(scope: RowScope, tint: Color) {
            scope.MenuItemOverflowMenu(tint, imageVector, contentDescription, content)
        }
    }
}

@Composable
fun RowScope.MenuItemOverflowMenu(
    tint: Color,
    imageVector: ImageVector,
    contentDescription: String = "",
    content: @Composable RowScope.(
        dismiss: () -> Unit
    ) -> Unit
) {
    val localThis = this
    val isDark = LocalDarkTheme.current
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(
            modifier = Modifier.size(24.dp),
            onClick = { showMenu = !showMenu }
        ) {
            Icon(
                tint = tint,
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
        DropdownMenu(
            modifier = Modifier
                .background(
                    if (isDark) {
                        Color.Black
                    } else {
                        Color.White
                    }
                )
                .padding(0.dp),
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            content(localThis) {
                showMenu = false
            }
        }
    }
}
