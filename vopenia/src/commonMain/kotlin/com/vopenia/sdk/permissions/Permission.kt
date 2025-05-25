package com.vopenia.sdk.permissions

import eu.codlab.permissions.bluetooth.PermissionBluetoothAdvertise
import eu.codlab.permissions.bluetooth.PermissionBluetoothConnect
import eu.codlab.permissions.bluetooth.PermissionBluetoothScan
import eu.codlab.permissions.camera.PermissionCamera
import eu.codlab.permissions.microphone.PermissionMicrophone

enum class Permission {
    CAMERA,
    RECORD_AUDIO,
    BLUETOOTH_SCAN,
    BLUETOOTH_ADVERTISE,
    BLUETOOTH_CONNECT;

    internal fun toPermission() = when (this) {
        CAMERA -> PermissionCamera
        RECORD_AUDIO -> PermissionMicrophone
        BLUETOOTH_SCAN -> PermissionBluetoothScan
        BLUETOOTH_ADVERTISE -> PermissionBluetoothAdvertise
        BLUETOOTH_CONNECT -> PermissionBluetoothConnect
    }
}
