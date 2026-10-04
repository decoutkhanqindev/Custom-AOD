package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue

sealed interface MainIntent {
    data class ToggleAod(val isEnabled: Boolean) : MainIntent
    data class ToggleDimBrightness(val isEnabled: Boolean) : MainIntent
    data class ToggleProximity(val isEnabled: Boolean) : MainIntent
    data class ChangeTimeout(val minutes: Int) : MainIntent
    data class ChangeMinBattery(val percent: Int) : MainIntent
    data class OpenPermission(val permission: PermissionValue) : MainIntent
    data object RefreshPermissions : MainIntent
    data object NotificationPermissionRequested : MainIntent
    data class NotificationPermissionResult(val isGranted: Boolean) : MainIntent
    data object Preview : MainIntent
}
