package io.vopenia.livekit.participant.effects

import platform.UIKit.UIImage

/**
 * Resolve a [BackgroundImage] to a UIKit [UIImage] for the iOS background-image
 * video processor. Bundled images are looked up by asset name; URI images are
 * read from a local file path. Returns null when the image can't be loaded.
 *
 * Shared by the published-track effect ([io.vopenia.livekit.participant.InternalLocalParticipant])
 * and the prejoin preview track ([io.vopenia.livekit.participant.track.local.LocalVideoTrackPreview]).
 */
internal fun loadBackgroundUiImage(image: BackgroundImage): UIImage? = when (image) {
    is BackgroundImage.Bundled -> UIImage.imageNamed(image.name)
    is BackgroundImage.Uri -> {
        val raw = image.uri
        val path = when {
            raw.startsWith("file://") -> raw.removePrefix("file://")
            raw.startsWith("/") -> raw
            else -> null
        }
        path?.let { UIImage(contentsOfFile = it) }
    }
}
