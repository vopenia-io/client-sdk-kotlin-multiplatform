package io.vopenia.app

import com.vopenia.sdk.Room
import eu.codlab.files.VirtualFile
import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch
import io.vopenia.app.http.BackendConnection
import io.vopenia.app.session.SavedSession
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private var baseUrl = com.vopenia.app.config.BuildKonfig.ENDPOINT_TOKEN

fun setEndpoint(base: String) {
    baseUrl = base
}

data class AppModelState(
    var initialized: Boolean = false,
    var loading: Boolean = false,
    val session: SavedSession? = null,
    val room: Room? = null
)

class AppModel() : StateViewModel<AppModelState>(AppModelState()) {
    private val backendConnection = BackendConnection(baseUrl)

    var onBackPressed: AppBackPressProvider = AppBackPressProvider()

    private val sessionFile = VirtualFile(VirtualFile.Root, "user.json")

    companion object {
        fun fake() = AppModel()
    }

    fun isInitialized() = states.value.initialized

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun initialize() = launch {
        val session = sessionFile.readStringIfExists()?.let {
            return@let Json.decodeFromString<SavedSession>(it)
        }

        println(session)

        delay(100.milliseconds)

        updateState {
            copy(
                initialized = true,
                session = session
            )
        }
    }

    fun joinRoom(participant: String, room: String) = launch {
        val roomObject = Room()
        updateState { copy(room = roomObject) }

        val token = backendConnection.token(participant, room)
        roomObject.connect(token.url, token.token)
    }
}

suspend fun VirtualFile.readStringIfExists() = if (exists()) {
    readString()
} else {
    null
}
