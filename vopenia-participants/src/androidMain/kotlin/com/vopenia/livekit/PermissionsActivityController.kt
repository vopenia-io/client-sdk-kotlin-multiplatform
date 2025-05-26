package com.vopenia.livekit

import androidx.fragment.app.FragmentActivity

object PermissionsActivityController {
    fun setActivity(activity: FragmentActivity) {
        eu.codlab.permissions.PermissionsController.setActivity(activity)
    }
}
