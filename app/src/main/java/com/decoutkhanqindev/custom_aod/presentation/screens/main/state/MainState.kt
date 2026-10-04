package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MainState(
    val isLoading: Boolean = true,
    val options: AodOptionsUiModel = AodOptionsUiModel(),
    val notificationOptions: AodNotificationOptionsUiModel = AodNotificationOptionsUiModel(),
    val rules: AodRulesUiModel = AodRulesUiModel(),
    val permissions: ImmutableList<PermissionUiModel> = persistentListOf(),
    val language: LanguageUiModel? = null,
    val editingScheduleTime: ScheduleTimeValue? = null,
    @param:StringRes val lastWakeMessageRes: Int = WakeResultValue.UNKNOWN.messageRes,
    val isNotificationPermissionPending: Boolean = false,
) {
    val isNotificationAccessGranted: Boolean
        get() = permissions.any { it.permission == PermissionValue.NOTIFICATION_ACCESS && it.isGranted == true }
}
