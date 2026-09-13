package io.vopenia.livekit.participant.delegate

import LiveKitClientKotlin.DelegateKotlin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * LiveKit dispatches every participant callback as an ObjC optional-protocol call, i.e.
 * `respondsToSelector:` then a message send. A Kotlin override whose exported selector
 * does not match the protocol's is therefore silently never called - the exact failure
 * shape of "mid-call publications never reach the app". These assertions pin the
 * selectors down.
 */
@OptIn(ExperimentalForeignApi::class)
class DelegateSelectorTest {
    private val selectors = listOf(
        "remoteParticipant:didPublishTrack:",
        "remoteParticipant:didUnpublishTrack:",
        "participant:didSubscribeTrack:",
        "participant:didUnsubscribeTrack:",
        "participant:trackPublication:didUpdateStreamState:",
        "participant:trackPublication:didUpdateIsMuted:",
        "participant:didUpdateIsSpeaking:",
        "participant:didUpdateAttributes:",
    )

    private fun remoteDelegate() = RemoteParticipantDelegate(
        onTrackPublished = {},
        onTrackUnpublished = {},
        onTrackSubscribed = {},
        onTrackUnsubscribed = {},
        onTrackPublicationIsMuted = { _, _ -> },
        onConnectionQuality = {},
        onIsSpeaking = {},
        onMetadataUpdated = {},
        onNameUpdated = {},
        onPermissionsUpdated = {},
        onAttributesUpdated = {},
        onTrackStreamStateChanged = { _, _ -> },
        onDataReceived = { _, _, _ -> },
    )

    @Test
    fun kotlinRemoteDelegateRespondsToEveryLiveKitSelector() {
        val delegate = remoteDelegate() as NSObject
        selectors.forEach { name ->
            assertTrue(
                delegate.respondsToSelector(NSSelectorFromString(name)),
                "Kotlin RemoteParticipantDelegate does not respond to $name",
            )
        }
    }

    @Test
    fun swiftShimRespondsToEveryLiveKitSelector() {
        val shim = DelegateKotlin().wrapParticipantDelegateWithDelegate(remoteDelegate()) as NSObject
        selectors.forEach { name ->
            assertTrue(
                shim.respondsToSelector(NSSelectorFromString(name)),
                "ParticipantDelegateKotlin does not respond to $name",
            )
        }
    }
}
