package com.vopenia.livekit

import LiveKitClient.ConnectOptions
import LiveKitClient.RemoteParticipant
import LiveKitClient.Room
import LiveKitClient.RoomDelegateProtocol
import LiveKitClient.RoomOptions
import LiveKitClient.addDelegate
import com.vopenia.livekit.events.ConnectionState
import com.vopenia.livekit.participant.InternalLocalParticipant
import com.vopenia.livekit.participant.InternalRemoteParticipant
import com.vopenia.livekit.room.RoomDelegateConnectionState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class RoomDelegate(
    private val scope: CoroutineScope,
    private val emit: (ConnectionState) -> Unit
) {
    private val connectOptions = ConnectOptions()
    private val roomOptions = RoomOptions()
    private val room: Room = Room(null, connectOptions, roomOptions)
    private val participants = MutableStateFlow<List<InternalRemoteParticipant>>(emptyList())

    val remoteParticipants = participants.asStateFlow()
    val localParticipant = InternalLocalParticipant(scope, room.localParticipant())

    @OptIn(ExperimentalForeignApi::class)
    suspend fun connectWithUrl(
        url: String,
        token: String,
    ) {
        suspendCoroutine { continuation ->
            room.connectWithUrl(
                url,
                token,
                null,
                null,
            ) { error ->
                if (null != error) {
                    continuation.resumeWithException(NSErrorException(error))
                } else {
                    continuation.resume(Unit)
                }
            }
        }

        room.remoteParticipants().values.forEach { onParticipantConnected(it as RemoteParticipant) }

        localParticipant.enableMicrophone(true)
    }

    private val delegates: List<RoomDelegateProtocol> = listOf(
        room.wrapDelegateWithDelegate(
            RoomDelegateConnectionState(
                emit,
                onParticipantConnected = { onParticipantConnected(it) },
                onParticipantDisconnected = { onParticipantDisconnected(it) }
            )
        ),
    )

    init {
        delegates.forEach {
            println("adding $it")
            room.addDelegate(it)
        }
    }

    fun disconnect() {
        room.disconnectWithCompletionHandler {
            // disconnected
        }
    }

    private fun onParticipantConnected(participant: RemoteParticipant) {
        scope.launch {
            val list = participants.value

            val identity = participant.identity()?.stringValue()

            list.find { it.identity == identity }.let {
                if (null == it) {
                    val newParticipant = InternalRemoteParticipant(scope, participant, true)
                    newParticipant.onConnect()

                    participants.emit(list + newParticipant)
                }
            }
        }
    }

    private fun onParticipantDisconnected(participant: RemoteParticipant) {
        scope.launch {
            val identity = participant.identity()?.stringValue()

            participants.value.find { it.identity == identity }?.onDisconnect()
        }
    }
}
