package com.decoutkhanqindev.custom_aod.presentation.screens.customize.state

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeDraftUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeGuideStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue

@Immutable
data class CustomizeState(
    val isLoading: Boolean = true,
    val nowMillis: Long = 0L,
    val draft: CustomizeDraftUiModel = CustomizeDraftUiModel(),
    val selectedTab: CustomizeTabValue? = null,
    val history: CustomizeHistoryState = CustomizeHistoryState(),
    val decor: CustomizeDecorState = CustomizeDecorState(),
    val effects: CustomizeEffectsState = CustomizeEffectsState(),
    val guideStep: CustomizeGuideStepValue? = null,
    val permissionRequest: PermissionValue? = null,
    val isApplying: Boolean = false,
)

@Immutable
data class CustomizeHistoryState(
    val hasChanges: Boolean = false,
    val isDefault: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isResetConfirmVisible: Boolean = false,
)

@Immutable
data class CustomizeDecorState(
    val isLoadingBackground: Boolean = false,
    val isDrawingPadVisible: Boolean = false,
    val isMemoEditorVisible: Boolean = false,
)

@Immutable
data class CustomizeEffectsState(
    val isGlowPreviewing: Boolean = false,
)
