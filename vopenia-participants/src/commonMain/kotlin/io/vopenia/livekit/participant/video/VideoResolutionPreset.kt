package io.vopenia.livekit.participant.video

/**
 * Resolution preset for the **sending** video pipeline (capture + publication).
 * Applied via `LocalParticipant.setMaxSendingResolution`. Mirrors Meet Web's
 * `features/settings/components/tabs/VideoTab.tsx` low/standard/high choice
 * and the LiveKit `VideoPresets43`/`VideoPresets169` families.
 */
enum class VideoResolutionPreset {
    /** ~180p — bandwidth-friendly, low-tier devices. */
    Low,
    /** ~360p — default trade-off. */
    Standard,
    /** ~720p — sharp video, higher bandwidth. */
    High
}

/**
 * Subscription quality cap applied to incoming **camera** tracks. Screen-share
 * tracks are intentionally not capped (the user wants them sharp). Applied via
 * `Room.setMaxReceivingQuality` and re-applied to any track that publishes
 * later in the call. Maps to LiveKit's `VideoQuality` enum.
 */
enum class VideoSubscribeQuality {
    /** Coarsest layer — saves bandwidth on tile-heavy grids. */
    Low,
    /** Mid layer. */
    Standard,
    /** Highest layer / no cap (publisher's auto-quality decides). */
    High
}
