package com.vopenia.sdk.permissions

import eu.codlab.permissions.PermissionState
import eu.codlab.permissions.PermissionState as PS

enum class PermissionState {
    NotDetermined,
    Granted,
    Denied,
    DeniedAlways,
    NotGranted;

    internal fun toState() = when (this) {
        NotDetermined -> PS.NotDetermined
        Granted -> PS.Granted
        Denied -> PS.Denied
        DeniedAlways -> PS.DeniedAlways
        NotGranted -> PS.NotGranted
    }

    companion object {
        internal fun from(state: PS) = when (state) {
            PS.NotDetermined -> NotDetermined
            PS.Granted -> Granted
            PS.Denied -> Denied
            PS.DeniedAlways -> DeniedAlways
            PermissionState.NotGranted -> NotGranted
        }
    }
}
