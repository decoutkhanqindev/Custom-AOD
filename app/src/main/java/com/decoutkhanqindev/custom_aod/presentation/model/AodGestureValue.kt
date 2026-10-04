package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager

// Mặc định giữ cách chạy trước đây: chạm 2 lần và phím back về màn hình khoá; phím âm lượng không gán thì vẫn chỉnh âm lượng.
@Immutable
enum class AodGestureValue(
    @param:StringRes val labelRes: Int,
    val defaultActionCode: Int,
) {
    DOUBLE_TAP(labelRes = R.string.aod_gesture_double_tap, defaultActionCode = DataStoreManager.DEFAULT_AOD_DOUBLE_TAP_ACTION),
    SWIPE_UP(labelRes = R.string.aod_gesture_swipe_up, defaultActionCode = DataStoreManager.DEFAULT_AOD_SWIPE_UP_ACTION),
    SWIPE_DOWN(labelRes = R.string.aod_gesture_swipe_down, defaultActionCode = DataStoreManager.DEFAULT_AOD_SWIPE_DOWN_ACTION),
    VOLUME_UP(labelRes = R.string.aod_gesture_volume_up, defaultActionCode = DataStoreManager.DEFAULT_AOD_VOLUME_UP_ACTION),
    VOLUME_DOWN(labelRes = R.string.aod_gesture_volume_down, defaultActionCode = DataStoreManager.DEFAULT_AOD_VOLUME_DOWN_ACTION),
    BACK(labelRes = R.string.aod_gesture_back, defaultActionCode = DataStoreManager.DEFAULT_AOD_BACK_ACTION),
}
