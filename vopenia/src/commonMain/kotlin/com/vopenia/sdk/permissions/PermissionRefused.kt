package com.vopenia.sdk.permissions

class PermissionRefused(val permission: Permission) : Throwable("Permission $permission refused")
