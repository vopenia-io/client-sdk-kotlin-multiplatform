package io.vopenia.livekit.participant.effects

/**
 * Source of a virtual-background image. The same instance is consumable from
 * any platform — interpretation of the string is platform-specific.
 */
sealed class BackgroundImage {
    /**
     * Image referenced by a URI string. Examples:
     * - Android: `file:///sdcard/bg.jpg`, `content://...`, `asset:///backgrounds/office.jpg`
     * - iOS: `file:///var/mobile/.../bg.jpg`
     */
    data class Uri(val uri: String) : BackgroundImage()

    /**
     * Image referenced by a resource name baked into the host application.
     * - Android: file inside the app's `assets/` directory (e.g. `"office.jpg"`).
     * - iOS: resource name in the app's main bundle (e.g. `"office"`, with extension
     *   resolved by `Bundle.url(forResource:withExtension:)`).
     */
    data class Bundled(val name: String) : BackgroundImage()
}
