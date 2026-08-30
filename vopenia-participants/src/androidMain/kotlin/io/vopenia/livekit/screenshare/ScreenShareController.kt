package io.vopenia.livekit.screenshare

import android.app.Notification
import android.content.Intent

/**
 * Bridge between the host Android application's MediaProjection permission flow
 * and the SDK's screen share API.
 *
 * Usage (in the host app, typically inside an Activity):
 * ```
 * val launcher = registerForActivityResult(StartActivityForResult()) { result ->
 *     if (result.resultCode == Activity.RESULT_OK) {
 *         ScreenShareController.setMediaProjectionResult(result.data!!)
 *         // then call room.localParticipant.startScreenShare() from a coroutine
 *     }
 * }
 * val intent = getSystemService(MediaProjectionManager::class.java)
 *     .createScreenCaptureIntent()
 * launcher.launch(intent)
 * ```
 *
 * Manifest pre-requisites:
 * `<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>`
 * `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"/>`
 * plus a foreground service component for screen capture.
 */
object ScreenShareController {
    private var resultData: Intent? = null
    private var notification: Notification? = null
    private var notificationId: Int? = null

    fun setMediaProjectionResult(
        intent: Intent,
        notification: Notification? = null,
        notificationId: Int? = null
    ) {
        this.resultData = intent
        this.notification = notification
        this.notificationId = notificationId
    }

    internal fun consume(): Triple<Intent, Notification?, Int?>? =
        resultData?.let { Triple(it, notification, notificationId) }

    fun clear() {
        resultData = null
        notification = null
        notificationId = null
    }
}
