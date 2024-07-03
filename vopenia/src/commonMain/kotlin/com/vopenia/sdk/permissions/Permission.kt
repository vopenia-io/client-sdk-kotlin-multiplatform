package com.vopenia.sdk.permissions

import eu.codlab.permissions.Permission as P

enum class Permission {
    CAMERA,
    RECORD_AUDIO,
    BLUETOOTH_SCAN,
    BLUETOOTH_ADVERTISE,
    BLUETOOTH_CONNECT;

    internal fun toPermission(): P {
        return when (this) {
            CAMERA -> P.CAMERA
            RECORD_AUDIO -> P.RECORD_AUDIO
            BLUETOOTH_SCAN -> P.BLUETOOTH_SCAN
            BLUETOOTH_ADVERTISE -> P.BLUETOOTH_ADVERTISE
            BLUETOOTH_CONNECT -> P.BLUETOOTH_CONNECT
        }
    }
}
