package com.decoutkhanqindev.custom_aod.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashDestination : NavKey

@Serializable
data object MainDestination : NavKey

@Serializable
data class LanguageDestination(val isFirstOpen: Boolean) : NavKey

@Serializable
data object CustomizeDestination : NavKey

@Serializable
data object PermissionDestination : NavKey

// TODO: Thêm destination của project — data object khi không có args, data class (field primitive/@Serializable) khi có args
