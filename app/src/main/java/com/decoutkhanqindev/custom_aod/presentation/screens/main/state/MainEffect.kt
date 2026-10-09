package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import androidx.annotation.StringRes

sealed interface MainEffect {
    sealed interface Service : MainEffect {
        data object StartAodService : Service
        data object StopAodService : Service
    }

    sealed interface Appearance : MainEffect {
        data object OpenBackgroundPicker : Appearance
    }

    sealed interface Permission : MainEffect {
        data object OpenOverlaySettings : Permission
        data object OpenMiuiPermissionSettings : Permission
        data object OpenNotificationSettings : Permission
        data object OpenNotificationAccessSettings : Permission
        data object OpenAppSettings : Permission
        data object RequestNotificationPermission : Permission
        data object RequestCalendarPermission : Permission
        data object RequestLocationPermission : Permission
    }

    sealed interface Navigation : MainEffect {
        data object NavigateToLanguage : Navigation
        data object OpenPreview : Navigation
    }

    data class ShowMessage(@param:StringRes val messageRes: Int) : MainEffect
}
