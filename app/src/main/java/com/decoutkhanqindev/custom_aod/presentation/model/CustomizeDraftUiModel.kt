package com.decoutkhanqindev.custom_aod.presentation.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager

// Chỉ ghi vào cài đặt thật khi bấm Áp dụng; mục cần quyền giữ giá trị có hiệu lực (đã bật và đã có quyền), mặc định = AOD trống.
@Immutable
data class CustomizeDraftUiModel(
    val appearance: CustomizeAppearanceUiModel = CustomizeAppearanceUiModel(),
    val decor: CustomizeDecorUiModel = CustomizeDecorUiModel(),
    val info: CustomizeInfoUiModel = CustomizeInfoUiModel(),
    val effects: CustomizeEffectsUiModel = CustomizeEffectsUiModel(),
) {
    val extras: AodExtrasUiModel
        get() = AodExtrasUiModel(
            memo = decor.memo,
            isCalendarEnabled = info.isCalendarEnabled,
            isWeatherEnabled = info.isWeatherEnabled,
            isWeatherFahrenheit = info.isWeatherFahrenheit,
            isDateEnabled = appearance.isDateEnabled,
            isBatteryEnabled = appearance.isBatteryEnabled,
        )
}

@Immutable
data class CustomizeAppearanceUiModel(
    val clock: AodAppearanceUiModel = AodAppearanceUiModel(),
    val isDateEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_DATE_ENABLED,
    val isBatteryEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_BATTERY_ENABLED,
)

// wallpaper và background loại trừ nhau: chọn cái này thì bỏ cái kia.
@Immutable
data class CustomizeDecorUiModel(
    val wallpaper: WallpaperValue? = null,
    val background: Bitmap? = null,
    val drawing: Bitmap? = null,
    val memo: String = DataStoreManager.DEFAULT_AOD_MEMO,
)

@Immutable
data class CustomizeInfoUiModel(
    val isNotificationIconsEnabled: Boolean = false,
    val isNotificationContentEnabled: Boolean = false,
    val isCalendarEnabled: Boolean = false,
    val isWeatherEnabled: Boolean = false,
    val isWeatherFahrenheit: Boolean = DataStoreManager.DEFAULT_IS_AOD_WEATHER_FAHRENHEIT,
    val isMediaControlsEnabled: Boolean = false,
)

@Immutable
data class CustomizeEffectsUiModel(
    val isEdgeGlowEnabled: Boolean = false,
)
