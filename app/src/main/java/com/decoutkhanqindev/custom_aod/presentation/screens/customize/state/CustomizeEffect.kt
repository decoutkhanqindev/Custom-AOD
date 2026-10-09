package com.decoutkhanqindev.custom_aod.presentation.screens.customize.state

import androidx.annotation.StringRes

sealed interface CustomizeEffect {
    sealed interface Decor : CustomizeEffect {
        data object OpenBackgroundPicker : Decor
    }

    sealed interface Permission : CustomizeEffect {
        data object RequestCalendarPermission : Permission
        data object RequestLocationPermission : Permission
        data object OpenNotificationAccessSettings : Permission
    }

    sealed interface Navigation : CustomizeEffect {
        data object NavigateToPermission : Navigation
        data object NavigateToMain : Navigation
        data object NavigateBack : Navigation
    }

    data class ShowMessage(@param:StringRes val messageRes: Int) : CustomizeEffect
}
