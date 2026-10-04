package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.StateFlow

@Immutable
data class AodInteractionUiModel(
    val actions: ImmutableMap<AodGestureValue, AodActionValue> = persistentMapOf(),
    val isAutoDimEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_AUTO_DIM_ENABLED,
    val isRaiseToWakeEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_RAISE_TO_WAKE_ENABLED,
) {
    fun actionOf(gesture: AodGestureValue): AodActionValue =
        actions[gesture] ?: AodActionValue.fromCode(gesture.defaultActionCode)
}

// Mỗi thao tác một key riêng trong DataStore: bảng ánh xạ đặt ở đây để màn cài đặt và AOD dùng chung.
fun DataStoreManager.gestureActionCode(gesture: AodGestureValue): StateFlow<Int?> = when (gesture) {
    AodGestureValue.DOUBLE_TAP -> aodDoubleTapAction
    AodGestureValue.SWIPE_UP -> aodSwipeUpAction
    AodGestureValue.SWIPE_DOWN -> aodSwipeDownAction
    AodGestureValue.VOLUME_UP -> aodVolumeUpAction
    AodGestureValue.VOLUME_DOWN -> aodVolumeDownAction
    AodGestureValue.BACK -> aodBackAction
}

fun DataStoreManager.saveGestureAction(gesture: AodGestureValue, action: AodActionValue) {
    when (gesture) {
        AodGestureValue.DOUBLE_TAP -> saveAodDoubleTapAction(action.code)
        AodGestureValue.SWIPE_UP -> saveAodSwipeUpAction(action.code)
        AodGestureValue.SWIPE_DOWN -> saveAodSwipeDownAction(action.code)
        AodGestureValue.VOLUME_UP -> saveAodVolumeUpAction(action.code)
        AodGestureValue.VOLUME_DOWN -> saveAodVolumeDownAction(action.code)
        AodGestureValue.BACK -> saveAodBackAction(action.code)
    }
}

fun DataStoreManager.currentAodInteraction(): AodInteractionUiModel = AodInteractionUiModel(
    actions = AodGestureValue.entries
        .associateWith { gesture ->
            AodActionValue.fromCode(gestureActionCode(gesture).value ?: gesture.defaultActionCode)
        }
        .toImmutableMap(),
    isAutoDimEnabled = isAodAutoDimEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_AUTO_DIM_ENABLED,
    isRaiseToWakeEnabled = isAodRaiseToWakeEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_RAISE_TO_WAKE_ENABLED,
)
