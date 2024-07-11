package com.vopenia.sdk

import androidx.fragment.app.FragmentActivity

object PermissionsActivityController {
    fun setActivity(activity: FragmentActivity) {
        eu.codlab.permissions.PermissionsController.setActivity(activity)
    }
}
