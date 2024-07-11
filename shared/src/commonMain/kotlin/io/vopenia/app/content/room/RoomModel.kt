package io.vopenia.app.content.room

import com.vopenia.sdk.Room
import com.vopenia.sdk.compose.log
import com.vopenia.sdk.participant.remote.RemoteParticipant
import com.vopenia.sdk.participant.track.RemoteVideoTrack
import com.vopenia.sdk.utils.Queue
import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch

data class RoomModelState(
    val participantCells: List<ParticipantCell> = emptyList()
)

class ParticipantCell(
    var participant: RemoteParticipant,
    var videoTrack: RemoteVideoTrack? = null,
    var sid: String? = null
)

class RoomModel(
    private val room: Room
) : StateViewModel<RoomModelState>(RoomModelState()) {
    private var knownParticipants: List<RemoteParticipant> = emptyList()
    private var knownTracks: List<RemoteVideoTrack> = emptyList()
    val queue = Queue()

    init {
        launch(
            onError = {
                log("RoomModel", "Error ${it.message}")
                it.printStackTrace()
            }
        ) {
            room.remoteParticipants.collect { participants ->
                participants.forEach { participant ->
                    println("remoteParticipants -> looping through")
                    if (null == knownParticipants.find { it.identity == participant.identity }) {
                        knownParticipants = knownParticipants + participant
                        launchForParticipant(participant)
                    }
                }
            }
        }
    }

    private suspend fun launchForParticipant(participant: RemoteParticipant) = launch(
        onError = {
            log("RoomModel", "Error launchForParticipant ${it.message}")
            it.printStackTrace()
        }
    ) {
        val originalTracks = states.value.participantCells

        if (null == originalTracks.find { it.participant == participant }) {
            println("launchForParticipant -> not found -> adding to cells")
            updateState {
                copy(
                    participantCells = participantCells + ParticipantCell(participant)
                )
            }
        }

        participant.videoTracks.collect { videoTracks ->
            videoTracks.forEach { videoTrack ->
                if (null == knownTracks.find { it.sid == videoTrack.sid }) {
                    knownTracks = knownTracks + videoTrack
                    launchForParticipantTrack(participant, videoTrack)
                }
            }
        }
    }

    private suspend fun launchForParticipantTrack(
        participant: RemoteParticipant,
        videoTrack: RemoteVideoTrack
    ) {
        launch(
            onError = {
                log("RoomModel", "Error launchForParticipantTrack ${it.message}")
                it.printStackTrace()
            }
        ) {
            videoTrack.state.collect { newState ->
                var originalTracks = states.value.participantCells
                println("launchForParticipant -> on video track ${videoTrack.sid} -> $newState")

                val empty = originalTracks.find {
                    it.participant.identity == participant.identity && it.sid == null
                }

                val matching = originalTracks.find {
                    it.participant.identity == participant.identity && it.sid == videoTrack.sid
                }

                if (null != matching) {
                    println("launchForParticipant -> this video track is known")
                } else if (null != empty) {
                    originalTracks = originalTracks - empty

                    println("launchForParticipant -> this video track is not known but we can squeeze it")
                    originalTracks = originalTracks + ParticipantCell(
                        participant,
                        videoTrack,
                        sid = videoTrack.sid
                    )
                } else {
                    println("launchForParticipant -> this video track is not known, we append it")
                    originalTracks = originalTracks + ParticipantCell(
                        participant,
                        videoTrack,
                        sid = videoTrack.sid
                    )
                }

                updateState {
                    copy(
                        participantCells = originalTracks
                    )
                }
            }
        }
    }
}
