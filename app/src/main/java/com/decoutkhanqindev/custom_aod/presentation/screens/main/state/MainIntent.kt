package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import android.graphics.Bitmap
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.ClockFontValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.ChargingRuleValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue

sealed interface MainIntent {
    sealed interface Options : MainIntent {
        data class ToggleCustomBrightness(val isEnabled: Boolean) : Options
        data class ChangeBrightness(val percent: Int) : Options
        data class ToggleProximity(val isEnabled: Boolean) : Options
        data class ChangeTimeout(val minutes: Int) : Options
    }

    sealed interface Appearance : MainIntent {
        data class ChangeClockFace(val face: ClockFaceValue) : Appearance
        data class ChangeClockFont(val font: ClockFontValue) : Appearance
        data class ChangeClockColor(val color: ClockColorValue) : Appearance
        data class ChangeClockSize(val percent: Int) : Appearance
        data class ToggleLandscape(val isEnabled: Boolean) : Appearance
        data class SelectWallpaper(val wallpaper: WallpaperValue) : Appearance
        data object OpenBackgroundPicker : Appearance
        data class BackgroundPickerResult(val uri: String?) : Appearance
        data object RemoveBackground : Appearance
    }

    sealed interface Extras : MainIntent {
        data class ToggleDate(val isEnabled: Boolean) : Extras
        data class ToggleBattery(val isEnabled: Boolean) : Extras
        data object ShowMemoEditor : Extras
        data object DismissMemoEditor : Extras
        data class ChangeMemo(val memo: String) : Extras
        data object ShowDrawingPad : Extras
        data object DismissDrawingPad : Extras
        data class ChangeDrawing(val drawing: Bitmap) : Extras
        data object RemoveDrawing : Extras
        data class ToggleCalendar(val isEnabled: Boolean) : Extras
        data class ToggleWeather(val isEnabled: Boolean) : Extras
        data class ToggleWeatherFahrenheit(val isEnabled: Boolean) : Extras
    }

    sealed interface Notifications : MainIntent {
        data class ToggleNotificationIcons(val isEnabled: Boolean) : Notifications
        data class ToggleNotificationContent(val isEnabled: Boolean) : Notifications
        data class ToggleEdgeGlow(val isEnabled: Boolean) : Notifications
        data class ToggleMediaControls(val isEnabled: Boolean) : Notifications
    }

    sealed interface Interaction : MainIntent {
        data class ShowGestureActionPicker(val gesture: AodGestureValue) : Interaction
        data object DismissGestureActionPicker : Interaction
        data class ChangeGestureAction(val gesture: AodGestureValue, val action: AodActionValue) :
            Interaction

        data class ToggleAutoDim(val isEnabled: Boolean) : Interaction
        data class ToggleRaiseToWake(val isEnabled: Boolean) : Interaction
    }

    sealed interface Rules : MainIntent {
        data class ChangeChargingRule(val rule: ChargingRuleValue) : Rules
        data class ToggleSchedule(val isEnabled: Boolean) : Rules
        data class ShowScheduleTimePicker(val time: ScheduleTimeValue) : Rules
        data object DismissScheduleTimePicker : Rules
        data class ChangeScheduleTime(val time: ScheduleTimeValue, val minuteOfDay: Int) : Rules
        data class ChangeMinBattery(val percent: Int) : Rules
    }

    sealed interface Permission : MainIntent {
        data class OpenPermissionSettings(val permission: PermissionValue) : Permission
        data object RefreshPermissions : Permission
        data object ShowPermissionSheet : Permission
        data object DismissPermissionSheet : Permission
        data class NotificationPermissionResult(val isGranted: Boolean) : Permission
        data class CalendarPermissionResult(val isGranted: Boolean) : Permission
        data class LocationPermissionResult(val isGranted: Boolean) : Permission
    }

    data class ToggleAod(val isEnabled: Boolean) : MainIntent
    data object NavigateToLanguage : MainIntent
    data object OpenPreview : MainIntent
}
