package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

sealed interface MainEffect {
    data object StartAodService : MainEffect
    data object StopAodService : MainEffect
    data object OpenPreview : MainEffect
    data object OpenOverlaySettings : MainEffect
    data object OpenMiuiPermissionEditor : MainEffect
    data object OpenNotificationSettings : MainEffect
    data object RequestNotificationPermission : MainEffect
}
