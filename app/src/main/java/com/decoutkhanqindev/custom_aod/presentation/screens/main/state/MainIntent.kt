package com.decoutkhanqindev.custom_aod.presentation.screens.main.state

import android.graphics.Bitmap
import com.decoutkhanqindev.custom_aod.presentation.model.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.ChargingRuleValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFontValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue

sealed interface MainIntent {
    data class ToggleAod(val isEnabled: Boolean) : MainIntent
    data class ToggleCustomBrightness(val isEnabled: Boolean) : MainIntent
    data class ChangeBrightness(val percent: Int) : MainIntent
    data class ToggleProximity(val isEnabled: Boolean) : MainIntent
    data class ChangeTimeout(val minutes: Int) : MainIntent
    data class ChangeClockFace(val face: ClockFaceValue) : MainIntent
    data class ChangeClockFont(val font: ClockFontValue) : MainIntent
    data class ChangeClockColor(val color: ClockColorValue) : MainIntent
    data class ChangeClockSize(val percent: Int) : MainIntent
    data class ToggleLandscape(val isEnabled: Boolean) : MainIntent
    data class SelectWallpaper(val wallpaper: WallpaperValue) : MainIntent
    data object OpenBackgroundPicker : MainIntent
    data class BackgroundPickerResult(val uri: String?) : MainIntent
    data object RemoveBackground : MainIntent
    data object ShowMemoEditor : MainIntent
    data object DismissMemoEditor : MainIntent
    data class ChangeMemo(val memo: String) : MainIntent
    data object ShowDrawingPad : MainIntent
    data object DismissDrawingPad : MainIntent
    data class ChangeDrawing(val drawing: Bitmap) : MainIntent
    data object RemoveDrawing : MainIntent
    data class ToggleCalendar(val isEnabled: Boolean) : MainIntent
    data class CalendarPermissionResult(val isGranted: Boolean) : MainIntent
    data class ToggleWeather(val isEnabled: Boolean) : MainIntent
    data class LocationPermissionResult(val isGranted: Boolean) : MainIntent
    data class ToggleWeatherFahrenheit(val isEnabled: Boolean) : MainIntent
    data class ShowGestureActionPicker(val gesture: AodGestureValue) : MainIntent
    data object DismissGestureActionPicker : MainIntent
    data class ChangeGestureAction(val gesture: AodGestureValue, val action: AodActionValue) : MainIntent
    data class ToggleAutoDim(val isEnabled: Boolean) : MainIntent
    data class ToggleRaiseToWake(val isEnabled: Boolean) : MainIntent
    data class ToggleNotificationIcons(val isEnabled: Boolean) : MainIntent
    data class ToggleNotificationContent(val isEnabled: Boolean) : MainIntent
    data class ToggleEdgeGlow(val isEnabled: Boolean) : MainIntent
    data class ToggleMediaControls(val isEnabled: Boolean) : MainIntent
    data class ChangeChargingRule(val rule: ChargingRuleValue) : MainIntent
    data class ToggleSchedule(val isEnabled: Boolean) : MainIntent
    data class ShowScheduleTimePicker(val time: ScheduleTimeValue) : MainIntent
    data object DismissScheduleTimePicker : MainIntent
    data class ChangeScheduleTime(val time: ScheduleTimeValue, val minuteOfDay: Int) : MainIntent
    data class ChangeMinBattery(val percent: Int) : MainIntent
    data class OpenPermissionSettings(val permission: PermissionValue) : MainIntent
    data object RefreshPermissions : MainIntent
    data object NotificationPermissionDialogShown : MainIntent
    data class NotificationPermissionResult(val isGranted: Boolean) : MainIntent
    data object NavigateToLanguage : MainIntent
    data object OpenPreview : MainIntent
}
