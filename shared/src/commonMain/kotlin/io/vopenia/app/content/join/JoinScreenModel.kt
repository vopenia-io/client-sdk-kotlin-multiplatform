package io.vopenia.app.content.join

import androidx.compose.ui.text.input.TextFieldValue
import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch
import io.vopenia.app.AppModel

data class JoinScreenModelState(
    val participant: TextFieldValue = TextFieldValue(),
    val room: TextFieldValue = TextFieldValue()
)

class JoinScreenModel(
    private val app: AppModel
) : StateViewModel<JoinScreenModelState>(JoinScreenModelState()) {
    var participant: TextFieldValue
        get() = states.value.participant
        set(value) {
            launch { updateState { copy(participant = value) } }
        }

    var room: TextFieldValue
        get() = states.value.room
        set(value) {
            launch { updateState { copy(room = value) } }
        }

    fun join() = app.joinRoom(participant.text, room.text)
}