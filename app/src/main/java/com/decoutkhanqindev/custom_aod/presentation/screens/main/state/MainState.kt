package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MainState(
    val isLoading: Boolean = true,
    val options: AodOptionsUiModel = AodOptionsUiModel(),
    val permissions: ImmutableList<PermissionUiModel> = persistentListOf(),
    @param:StringRes val lastWakeMessageRes: Int = WakeResultValue.UNKNOWN.messageRes,
    val isNotificationPermissionPending: Boolean = false,
)
