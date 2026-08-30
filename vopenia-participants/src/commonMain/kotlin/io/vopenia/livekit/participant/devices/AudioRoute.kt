package io.vopenia.livekit.participant.devices

/**
 * In-call audio output route.
 *
 * On every platform the route also determines mic INPUT in practice (a
 * Bluetooth headset both captures and plays; the earpiece/speaker use the
 * built-in mic), so this models the whole communication routing — not just the
 * speaker. The proximity sensor is enabled only for [Earpiece].
 */
enum class AudioRoute {
    /** Phone receiver, held to the ear. Proximity sensor on. */
    Earpiece,

    /** External loudspeaker. */
    Speaker,

    /** Connected Bluetooth headset — output and mic. */
    Bluetooth,
}
