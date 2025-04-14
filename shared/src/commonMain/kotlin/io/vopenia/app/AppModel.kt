package io.vopenia.app

import androidx.compose.material.ScaffoldState
import com.vopenia.sdk.Room
import eu.codlab.files.VirtualFile
import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch
import io.vopenia.app.content.navigation.NavigateTo
import io.vopenia.app.http.BackendConnection
import io.vopenia.app.session.SavedSession
import io.vopenia.app.utils.safeLaunch
import io.vopenia.app.widgets.AppBarState
import io.vopenia.app.widgets.FloatingActionButtonState
import io.vopenia.shared.config.BuildKonfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import moe.tlaster.precompose.navigation.Navigator
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private var baseUrl = BuildKonfig.ENDPOINT_TOKEN

fun setEndpoint(base: String) {
    baseUrl = base
}

data class AppModelState(
    var currentRoute: NavigateTo,
    val appBarState: AppBarState = AppBarState.Hidden,
    val floatingActionButtonState: FloatingActionButtonState? = null,

    var initialized: Boolean = false,
    var loading: Boolean = false,
    val session: SavedSession? = null,
    val room: Room? = null
)

interface AppModel {
    val states: StateFlow<AppModelState>

    var navigator: Navigator?
    var scaffoldState: ScaffoldState?

    var onBackPressed: AppBackPressProvider

    fun isInitialized(): Boolean

    fun initialize()

    fun joinRoom(participant: String, room: String)

    fun leaveRoom()

    fun show(navigateTo: NavigateTo)

    fun setAppBarState(appBarState: AppBarState)
}

class AppModelPreview : AppModel {
    override var navigator: Navigator? = null
    override var scaffoldState: ScaffoldState? = null
    override val states = MutableStateFlow(AppModelState(NavigateTo.Initialize))

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

    override fun leaveRoom() {
        // nothing
    }

    override fun show(navigateTo: NavigateTo) {
        // nothing
    }

    override fun setAppBarState(appBarState: AppBarState) {
        // nothing
    }
}

class AppModelImpl : StateViewModel<AppModelState>(AppModelState(NavigateTo.Initialize)),
    AppModel {
    override var navigator: Navigator? = null
    override var scaffoldState: ScaffoldState? = null

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

                show(NavigateTo.Main())
            } catch (err: Throwable) {
                err.printStackTrace()
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

    override fun leaveRoom() {
        launch {
            states.value.room?.disconnect()
            updateState { copy(room = null) }
        }
    }

    override fun show(navigateTo: NavigateTo) {
        println("SHowing $navigateTo")
        if (navigateTo.popBackStack) {
            popBackStack()
        }

        navigator?.navigate(
            route = navigateTo.route,
            options = navigateTo.options
        )

        safeLaunch {
            scaffoldState?.drawerState?.close()
        }

        updateState {
            copy(
                currentRoute = navigateTo,
                appBarState = AppBarState.Hidden,
                floatingActionButtonState = null
            )
        }
    }

    override fun setAppBarState(appBarState: AppBarState) {
        safeLaunch {
            updateState {
                copy(appBarState = appBarState)
            }
        }
    }

    fun popBackStack() {
        navigator?.popBackStack()
    }
}

suspend fun VirtualFile.readStringIfExists() = if (exists()) {
    readString()
} else {
    null
}
