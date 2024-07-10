package com.vopenia.sdk.participant.track

expect sealed class RemoteTrack : SubRemoteTrack {
    internal val track: RemoteTrackPublication

    val name: String

    val isEnabled: Boolean

    val isSubscriptionAllowed: Boolean

    val kind: Kind

    val sid: String

    suspend fun enable(enable: Boolean)

    suspend fun subscribe(subscribe: Boolean)

    internal fun updateInternalTrack(track: RemoteTrackPublication)
}
