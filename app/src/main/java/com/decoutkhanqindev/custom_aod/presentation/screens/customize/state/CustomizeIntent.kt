package com.decoutkhanqindev.custom_aod.presentation.screens.customize.state

import android.graphics.Bitmap
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFontValue
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue

sealed interface CustomizeIntent {
    sealed interface Panel : CustomizeIntent {
        data class SelectTab(val tab: CustomizeTabValue) : Panel
        data object ClosePanel : Panel
    }

    sealed interface Appearance : CustomizeIntent {
        data class ChangeClockFace(val face: ClockFaceValue) : Appearance
        data class ChangeClockFont(val font: ClockFontValue) : Appearance
        data class ChangeClockColor(val color: ClockColorValue) : Appearance
        data class ChangeClockSize(val percent: Int) : Appearance
        data class ToggleDate(val isEnabled: Boolean) : Appearance
        data class ToggleBattery(val isEnabled: Boolean) : Appearance
    }

    sealed interface Decor : CustomizeIntent {
        data class SelectWallpaper(val wallpaper: WallpaperValue) : Decor
        data object OpenBackgroundPicker : Decor
        data class BackgroundPickerResult(val uri: String?) : Decor
        data object RemoveBackground : Decor
        data object ShowDrawingPad : Decor
        data object DismissDrawingPad : Decor
        data class ChangeDrawing(val drawing: Bitmap) : Decor
        data object RemoveDrawing : Decor
        data object ShowMemoEditor : Decor
        data object DismissMemoEditor : Decor
        data class ChangeMemo(val memo: String) : Decor
    }

    sealed interface Info : CustomizeIntent {
        data class ToggleNotificationIcons(val isEnabled: Boolean) : Info
        data class ToggleNotificationContent(val isEnabled: Boolean) : Info
        data class ToggleCalendar(val isEnabled: Boolean) : Info
        data class ToggleWeather(val isEnabled: Boolean) : Info
        data class ToggleWeatherFahrenheit(val isEnabled: Boolean) : Info
        data class ToggleMediaControls(val isEnabled: Boolean) : Info
    }

    sealed interface Effects : CustomizeIntent {
        data class ToggleEdgeGlow(val isEnabled: Boolean) : Effects
        data object PreviewEdgeGlow : Effects
    }

    sealed interface History : CustomizeIntent {
        data object UndoChange : History
        data object RedoChange : History
        data object ShowResetConfirm : History
        data object DismissResetConfirm : History
        data object ResetCustomization : History
    }

    sealed interface Permission : CustomizeIntent {
        data class OpenPermissionSettings(val permission: PermissionValue) : Permission
        data object DismissPermissionSheet : Permission
        data class CalendarPermissionResult(val isGranted: Boolean) : Permission
        data class LocationPermissionResult(val isGranted: Boolean) : Permission
        data object RefreshPermissions : Permission
    }

    sealed interface Guide : CustomizeIntent {
        data object ShowGuide : Guide
        data object ShowNextGuideStep : Guide
        data object DismissGuide : Guide
    }

    data object ConfirmCustomization : CustomizeIntent
    data object SkipCustomization : CustomizeIntent
    data object NavigateBack : CustomizeIntent
}
