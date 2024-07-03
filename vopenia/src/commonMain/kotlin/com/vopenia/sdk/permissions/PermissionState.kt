package com.vopenia.sdk.permissions

import eu.codlab.permissions.PermissionState as PS

enum class PermissionState {
    NotDetermined,
    Granted,
    Denied,
    DeniedAlways;

    internal fun toState() = when (this) {
        NotDetermined -> PS.NotDetermined
        Granted -> PS.Granted
        Denied -> PS.Denied
        DeniedAlways -> PS.DeniedAlways
    }

    companion object {
        internal fun from(state: PS) = when (state) {
            PS.NotDetermined -> NotDetermined
            PS.Granted -> Granted
            PS.Denied -> Denied
            PS.DeniedAlways -> DeniedAlways
        }
    }
}