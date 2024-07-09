package io.vopenia.app

import com.vopenia.sdk.Room
import eu.codlab.files.VirtualFile
import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch
import io.vopenia.app.http.BackendConnection
import io.vopenia.app.session.SavedSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

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

interface AppModel {
    val states: StateFlow<AppModelState>

    var onBackPressed: AppBackPressProvider

    fun isInitialized(): Boolean

    fun initialize()

    fun joinRoom(participant: String, room: String)
}

class AppModelPreview : AppModel {
    override val states = MutableStateFlow(AppModelState())

    override var onBackPressed = AppBackPressProvider()

    override fun isInitialized(): Boolean {
        return false
    }

    override fun initialize() {
        // nothing
    }

    override fun joinRoom(participant: String, room: String) {
        // nothing
    }

}

class AppModelImpl() : StateViewModel<AppModelState>(AppModelState()), AppModel {
    private val backendConnection = BackendConnection(baseUrl)

    override var onBackPressed: AppBackPressProvider = AppBackPressProvider()

    private val sessionFile = VirtualFile(VirtualFile.Root, "user.json")

    companion object {
        fun fake() = AppModelImpl()
    }

    override fun isInitialized() = states.value.initialized

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun initialize() {
        launch {
            try {
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
            } catch (err: Throwable) {
                //
            }
        }
    }

    override fun joinRoom(participant: String, room: String) {
        launch {
            val roomObject = Room()
            updateState { copy(room = roomObject) }

            val token = backendConnection.token(participant, room)
            roomObject.connect(token.url, token.token)
        }
    }
}

suspend fun VirtualFile.readStringIfExists() = if (exists()) {
    readString()
} else {
    null
}
