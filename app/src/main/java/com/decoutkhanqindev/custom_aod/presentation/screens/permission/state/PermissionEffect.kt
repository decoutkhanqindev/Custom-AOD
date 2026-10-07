package com.decoutkhanqindev.custom_aod.presentation.screens.permission.state

sealed interface PermissionEffect {
    data object OpenOverlaySettings : PermissionEffect
    data object OpenMiuiPermissionSettings : PermissionEffect
    data object OpenNotificationSettings : PermissionEffect
    data object RequestNotificationPermission : PermissionEffect
    data object StartAodService : PermissionEffect
    data object NavigateToMain : PermissionEffect
    data object NavigateBack : PermissionEffect
}
