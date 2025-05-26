package io.vopenia.app.content.pages.room

import com.vopenia.livekit.Room
import com.vopenia.livekit.participant.Participant
import com.vopenia.livekit.participant.local.LocalParticipant
import com.vopenia.livekit.participant.remote.RemoteParticipant
import com.vopenia.livekit.participant.track.IVideoTrack
import com.vopenia.livekit.participant.track.RemoteVideoTrack
import com.vopenia.livekit.participant.track.Source
import com.vopenia.livekit.participant.track.Track
import com.vopenia.livekit.participant.track.local.LocalVideoTrack
import com.vopenia.livekit.permissions.PermissionRefused
import com.vopenia.livekit.permissions.PermissionUnrecoverable
import com.vopenia.livekit.permissions.PermissionsController
import com.vopenia.sdk.utils.Log
import com.vopenia.sdk.utils.Queue
import eu.codlab.viewmodel.StateViewModel
import eu.codlab.viewmodel.launch
import kotlinx.coroutines.launch

data class RoomModelState(
    val localParticipantCells: List<LocalParticipantCell> = emptyList(),
    val participantCells: List<RemoteParticipantCell> = emptyList()
)

sealed class ParticipantCellHolder<T : IVideoTrack>(
    var videoTrack: T? = null,
    var sid: String? = null,
    var source: Source,
) {
    override fun equals(other: Any?): Boolean {
        if (other is ParticipantCellHolder<*>) {
            return other.sid == sid && other.source == source
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}

class LocalParticipantCell(
    videoTrack: LocalVideoTrack? = null,
    sid: String? = null,
    source: Source = Source.CAMERA
) : ParticipantCellHolder<LocalVideoTrack>(videoTrack, sid, source)

class RemoteParticipantCell(
    var participant: RemoteParticipant,
    videoTrack: RemoteVideoTrack? = null,
    sid: String? = null,
    source: Source
) : ParticipantCellHolder<RemoteVideoTrack>(videoTrack, sid, source)

class RoomModel(
    private val room: Room
) : StateViewModel<RoomModelState>(
    RoomModelState(
        localParticipantCells = listOf(LocalParticipantCell())
    )
) {
    private var knownParticipants: List<RemoteParticipant> = emptyList()
    private var knownTracks: List<RemoteVideoTrack> = emptyList()
    private var knownLocalTracks: List<LocalVideoTrack> = emptyList()
    private var microphoneChecker = TrackAvailabilityChecker(viewModelScope, Source.MICROPHONE)
    private var videoChecker = TrackAvailabilityChecker(viewModelScope, Source.CAMERA)
    val queue = Queue()

    val microphoneEnabledState = microphoneChecker.hasExpectedSource
    val cameraEnabledState = videoChecker.hasExpectedSource

    init {
        launch(
            onError = {
                Log.d("RoomModel", "Error ${it.message}")
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

        launch(
            onError = {
                // nothing
            }
        ) {
            room.localParticipant.videoTracks.collect { videoTracks ->
                videoTracks.forEach { videoTrack ->
                    videoChecker.checkAndStart(videoTrack)

                    if (null == knownLocalTracks.find { it.sid == videoTrack.sid }) {
                        knownLocalTracks = knownLocalTracks + videoTrack
                        launchForLocalParticipantTrack(room.localParticipant, videoTrack)
                    }
                }
            }
        }

        launch(
            onError = {
                // nothing here
            }
        ) {
            room.localParticipant.audioTracks.collect { tracks ->
                tracks.forEach { track ->
                    Log.d("RoomModel", "collect an audio track")
                    microphoneChecker.checkAndStart(track)
                }
            }
        }
    }

    private suspend fun launchForParticipant(participant: RemoteParticipant) = launch(
        onError = {
            Log.d("RoomModel", "Error launchForParticipant ${it.message}")
            it.printStackTrace()
        }
    ) {
        val originalTracks = states.value.participantCells

        if (null == originalTracks.find { it.participant == participant }) {
            println("launchForParticipant -> not found -> adding to cells")
            updateState {
                copy(
                    participantCells = participantCells + RemoteParticipantCell(
                        participant,
                        source = Source.CAMERA
                    )
                )
            }
        }

        launch {
            participant.state.collect {
                if (it.connected) return@collect

                updateState {
                    copy(
                        participantCells = participantCells.filter { cell ->
                            cell.participant != participant
                        }
                    )
                }
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

    private suspend fun launchForLocalParticipantTrack(
        participant: LocalParticipant,
        givenVideoTrack: LocalVideoTrack
    ) {
        launchForParticipantTrack(
            participant,
            givenVideoTrack,
            cellList = { states.value.localParticipantCells },
            isEmpty = { it.sid == null },
            isMatchingSid = { it.sid == givenVideoTrack.sid },
            createCell = { _, videoTrack ->
                LocalParticipantCell(
                    videoTrack,
                    sid = videoTrack.sid,
                    source = videoTrack.source
                )
            },
            copyState = {
                copy(
                    localParticipantCells = it
                )
            }
        )
    }

    private suspend fun launchForParticipantTrack(
        givenParticipant: RemoteParticipant,
        givenVideoTrack: RemoteVideoTrack
    ) {
        launchForParticipantTrack(
            givenParticipant,
            givenVideoTrack,
            cellList = { states.value.participantCells },
            isEmpty = { it.participant.identity == givenParticipant.identity && it.sid == null },
            isMatchingSid = { it.participant.identity == givenParticipant.identity && it.sid == givenVideoTrack.sid },
            createCell = { participant, videoTrack ->
                RemoteParticipantCell(
                    participant,
                    videoTrack,
                    sid = videoTrack.sid,
                    source = videoTrack.source
                )
            },
            copyState = {
                copy(
                    participantCells = it
                )
            }
        )
    }

    private suspend fun <P : Participant<*, *, *, *>, V : Track, C : ParticipantCellHolder<*>> launchForParticipantTrack(
        participant: P,
        videoTrack: V,
        cellList: () -> List<C>,
        isEmpty: (C) -> Boolean,
        isMatchingSid: (C) -> Boolean,
        createCell: (P, V) -> C,
        copyState: RoomModelState.(List<C>) -> RoomModelState
    ) {
        launch(
            onError = {
                Log.d("RoomModel", "Error launchForParticipantTrack ${it.message}")
                it.printStackTrace()
            }
        ) {
            videoTrack.state.collect { newState ->
                var originalTracks = cellList()

                val empty = originalTracks.find { isEmpty(it) }

                val matching = originalTracks.find { isMatchingSid(it) }

                Log.d("RoomModel", "newState $newState")

                if (null != matching) {
                    // if the track is unpublished && is not a camera
                    if (matching.source == Source.SCREEN_SHARE) {
                        if (!newState.published || newState.muted) {
                            originalTracks = originalTracks - matching
                        }
                    }
                } else if (!newState.published) {
                    // we didn't find a cell but the participant is not publishing so we skip
                } else if (null != empty) {
                    originalTracks = originalTracks - empty

                    originalTracks = originalTracks + createCell(participant, videoTrack)
                } else {
                    originalTracks = originalTracks + createCell(participant, videoTrack)
                }

                updateState {
                    copyState(originalTracks)
                }
            }
        }
    }

    @Suppress("SwallowedException")
    fun enableMicrophone(enable: Boolean) = launch {
        try {
            room.localParticipant.enableMicrophone(enable)
        } catch (err: PermissionRefused) {
            // refused -> show popup
        } catch (err: PermissionUnrecoverable) {
            // todo : app setting
            PermissionsController.openAppSettings()
        }
    }

    @Suppress("SwallowedException")
    fun enableCamera(enable: Boolean) = launch {
        try {
            room.localParticipant.enableCamera(enable)
        } catch (err: PermissionRefused) {
            // refused -> show popup
        } catch (err: PermissionUnrecoverable) {
            // todo : app setting
            PermissionsController.openAppSettings()
        }
    }
}
