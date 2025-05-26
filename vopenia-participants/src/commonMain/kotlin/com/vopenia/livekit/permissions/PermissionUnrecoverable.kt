package com.vopenia.livekit.permissions

class PermissionUnrecoverable(val permission: Permission) :
    Throwable("Permission $permission needs to move to the app settings")
