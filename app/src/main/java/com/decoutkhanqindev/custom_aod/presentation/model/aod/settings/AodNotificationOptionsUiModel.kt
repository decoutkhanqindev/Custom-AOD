package com.decoutkhanqindev.custom_aod.presentation.model.aod.settings

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

@Immutable
data class AodNotificationOptionsUiModel(
    val isIconsEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_NOTIFICATION_ICONS_ENABLED,
    val isContentEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_NOTIFICATION_CONTENT_ENABLED,
    val isEdgeGlowEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_EDGE_GLOW_ENABLED,
    val isMediaControlsEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_MEDIA_CONTROLS_ENABLED,
)

fun DataStoreManager.currentAodNotificationOptions(): AodNotificationOptionsUiModel =
    AodNotificationOptionsUiModel(
        isIconsEnabled = isAodNotificationIconsEnabled.value
            ?: DataStoreManager.DEFAULT_IS_AOD_NOTIFICATION_ICONS_ENABLED,
        isContentEnabled = isAodNotificationContentEnabled.value
            ?: DataStoreManager.DEFAULT_IS_AOD_NOTIFICATION_CONTENT_ENABLED,
        isEdgeGlowEnabled = isAodEdgeGlowEnabled.value
            ?: DataStoreManager.DEFAULT_IS_AOD_EDGE_GLOW_ENABLED,
        isMediaControlsEnabled = isAodMediaControlsEnabled.value
            ?: DataStoreManager.DEFAULT_IS_AOD_MEDIA_CONTROLS_ENABLED,
    )

fun DataStoreManager.observeAodNotificationOptions(): Flow<AodNotificationOptionsUiModel> = combine(
    isAodNotificationIconsEnabled.filterNotNull(),
    isAodNotificationContentEnabled.filterNotNull(),
    isAodEdgeGlowEnabled.filterNotNull(),
    isAodMediaControlsEnabled.filterNotNull(),
) { isIconsEnabled, isContentEnabled, isEdgeGlowEnabled, isMediaControlsEnabled ->
    AodNotificationOptionsUiModel(
        isIconsEnabled = isIconsEnabled,
        isContentEnabled = isContentEnabled,
        isEdgeGlowEnabled = isEdgeGlowEnabled,
        isMediaControlsEnabled = isMediaControlsEnabled,
    )
}
