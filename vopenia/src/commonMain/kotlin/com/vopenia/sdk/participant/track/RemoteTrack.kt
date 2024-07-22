package com.vopenia.sdk.participant.track

expect sealed class RemoteTrack : Track {
    internal val track: RemoteTrackPublication

    override val name: String

    override val source: Source

    val isEnabled: Boolean

    val isSubscriptionAllowed: Boolean

    override val kind: Kind

    override val sid: String

    suspend fun enable(enable: Boolean)

    suspend fun subscribe(subscribe: Boolean)

    internal fun updateInternalTrack(track: RemoteTrackPublication)
}
