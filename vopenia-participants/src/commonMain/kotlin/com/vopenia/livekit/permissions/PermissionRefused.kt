package com.vopenia.livekit.permissions

class PermissionRefused(val permission: Permission) : Throwable("Permission $permission refused")
