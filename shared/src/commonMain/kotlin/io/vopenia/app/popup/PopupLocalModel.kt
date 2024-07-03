package io.vopenia.app.popup


import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch
import korlibs.io.util.UUID

data class PopupStateHolder(
    val uuid: String = UUID.randomUUID().toString(),
    var title: String = "",
    var text: String = "",
    val onConfirm: () -> Unit = {},
    val onDismiss: (() -> Unit)? = null
)

data class PopupLocalModelState(
    var show: Boolean = false,
    val invocations: List<PopupStateHolder> = mutableListOf()
)

class PopupLocalModel : StateViewModel<PopupLocalModelState>(PopupLocalModelState()) {
    fun show(
        title: String,
        text: String,
        onDismiss: (() -> Unit)? = null,
        onConfirm: () -> Unit = {}
    ) = launch {
        updateState {
            copy(
                show = true,
                invocations = invocations +
                        PopupStateHolder(
                            title = title,
                            text = text,
                            onDismiss = onDismiss,
                            onConfirm = onConfirm
                        )
            )
        }
    }

    fun dismiss(uuid: String) = launch {
        val newInvocations = states.value.invocations.filter {
            it.uuid != uuid
        }
        updateState {
            copy(
                show = newInvocations.isNotEmpty(),
                invocations = newInvocations
            )
        }
    }
}
