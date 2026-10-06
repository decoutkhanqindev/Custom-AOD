package com.decoutkhanqindev.custom_aod.presentation.screens.permission.state

import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue

sealed interface PermissionIntent {
    data object RefreshPermissions : PermissionIntent
    data class OpenPermissionSettings(val permission: PermissionValue) : PermissionIntent
    data object ConfirmPermissions : PermissionIntent
}
