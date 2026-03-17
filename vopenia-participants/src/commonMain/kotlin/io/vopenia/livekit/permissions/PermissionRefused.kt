package io.vopenia.livekit.permissions

class PermissionRefused(val permission: Permission) : Throwable("Permission $permission refused")
