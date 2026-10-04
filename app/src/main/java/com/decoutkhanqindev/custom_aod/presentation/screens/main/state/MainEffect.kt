package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import androidx.annotation.StringRes

sealed interface MainEffect {
    data object StartAodService : MainEffect
    data object StopAodService : MainEffect
    data object OpenPreview : MainEffect
    data object OpenOverlaySettings : MainEffect
    data object OpenMiuiPermissionSettings : MainEffect
    data object OpenNotificationSettings : MainEffect
    data object OpenNotificationAccessSettings : MainEffect
    data object RequestNotificationPermission : MainEffect
    data object RequestCalendarPermission : MainEffect
    data object RequestLocationPermission : MainEffect
    data object NavigateToLanguage : MainEffect
    data object OpenBackgroundPicker : MainEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : MainEffect
}
