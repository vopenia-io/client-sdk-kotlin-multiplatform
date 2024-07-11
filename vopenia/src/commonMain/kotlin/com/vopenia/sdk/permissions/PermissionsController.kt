package com.vopenia.sdk.permissions

import eu.codlab.permissions.PermissionsController as PC

object PermissionsController {
    private val controller = PC()

    suspend fun providePermission(permission: Permission) {
        controller.providePermission(permission.toPermission())
    }

    suspend fun isGranted(permission: Permission) = controller.isGranted(permission.toPermission())

    suspend fun getState(permission: Permission): PermissionState {
        return PermissionState.from(controller.getState(permission.toPermission()))
    }

    fun canOpenAppSettings() = controller.canOpenAppSettings()

    fun openAppSettings() = controller.openAppSettings()
}
