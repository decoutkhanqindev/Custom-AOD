package com.decoutkhanqindev.custom_aod.data.local.datastore

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.recoverCatching
import com.decoutkhanqindev.custom_aod.utils.withContextCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class DataStoreManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val Context.prefs: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

    private val selectedLangCodeKey: Preferences.Key<String> =
        stringPreferencesKey(SELECTED_LANG_CODE_KEY)
    val selectedLangCode: StateFlow<String?> =
        selectedLangCodeKey.asStateFlow(default = DEFAULT_SELECTED_LANG_CODE)

    private val isFirstOpenKey: Preferences.Key<Boolean> = booleanPreferencesKey(IS_FIRST_OPEN_KEY)
    val isFirstOpen: StateFlow<Boolean?> =
        isFirstOpenKey.asStateFlow(default = DEFAULT_IS_FIRST_OPEN)

    private val isAodEnabledKey: Preferences.Key<Boolean> = booleanPreferencesKey(IS_AOD_ENABLED_KEY)
    val isAodEnabled: StateFlow<Boolean?> =
        isAodEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_ENABLED)

    private val isAodCustomBrightnessKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_CUSTOM_BRIGHTNESS_KEY)
    val isAodCustomBrightness: StateFlow<Boolean?> =
        isAodCustomBrightnessKey.asStateFlow(default = DEFAULT_IS_AOD_CUSTOM_BRIGHTNESS)

    private val aodBrightnessPercentKey: Preferences.Key<Int> = intPreferencesKey(AOD_BRIGHTNESS_PERCENT_KEY)
    val aodBrightnessPercent: StateFlow<Int?> =
        aodBrightnessPercentKey.asStateFlow(default = DEFAULT_AOD_BRIGHTNESS_PERCENT)

    private val isAodProximityEnabledKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_PROXIMITY_ENABLED_KEY)
    val isAodProximityEnabled: StateFlow<Boolean?> =
        isAodProximityEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_PROXIMITY_ENABLED)

    private val aodTimeoutMinutesKey: Preferences.Key<Int> = intPreferencesKey(AOD_TIMEOUT_MINUTES_KEY)
    val aodTimeoutMinutes: StateFlow<Int?> =
        aodTimeoutMinutesKey.asStateFlow(default = DEFAULT_AOD_TIMEOUT_MINUTES)

    private val aodMinBatteryKey: Preferences.Key<Int> = intPreferencesKey(AOD_MIN_BATTERY_KEY)
    val aodMinBattery: StateFlow<Int?> =
        aodMinBatteryKey.asStateFlow(default = DEFAULT_AOD_MIN_BATTERY)

    private val aodChargingRuleKey: Preferences.Key<Int> = intPreferencesKey(AOD_CHARGING_RULE_KEY)
    val aodChargingRule: StateFlow<Int?> =
        aodChargingRuleKey.asStateFlow(default = DEFAULT_AOD_CHARGING_RULE)

    private val isAodScheduleEnabledKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_SCHEDULE_ENABLED_KEY)
    val isAodScheduleEnabled: StateFlow<Boolean?> =
        isAodScheduleEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_SCHEDULE_ENABLED)

    private val aodScheduleStartMinuteKey: Preferences.Key<Int> =
        intPreferencesKey(AOD_SCHEDULE_START_MINUTE_KEY)
    val aodScheduleStartMinute: StateFlow<Int?> =
        aodScheduleStartMinuteKey.asStateFlow(default = DEFAULT_AOD_SCHEDULE_START_MINUTE)

    private val aodScheduleEndMinuteKey: Preferences.Key<Int> = intPreferencesKey(AOD_SCHEDULE_END_MINUTE_KEY)
    val aodScheduleEndMinute: StateFlow<Int?> =
        aodScheduleEndMinuteKey.asStateFlow(default = DEFAULT_AOD_SCHEDULE_END_MINUTE)

    private val isAodNotificationIconsEnabledKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_NOTIFICATION_ICONS_ENABLED_KEY)
    val isAodNotificationIconsEnabled: StateFlow<Boolean?> =
        isAodNotificationIconsEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_NOTIFICATION_ICONS_ENABLED)

    private val isAodEdgeGlowEnabledKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_EDGE_GLOW_ENABLED_KEY)
    val isAodEdgeGlowEnabled: StateFlow<Boolean?> =
        isAodEdgeGlowEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_EDGE_GLOW_ENABLED)

    private val isAodMediaControlsEnabledKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_MEDIA_CONTROLS_ENABLED_KEY)
    val isAodMediaControlsEnabled: StateFlow<Boolean?> =
        isAodMediaControlsEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_MEDIA_CONTROLS_ENABLED)

    private val aodClockFaceKey: Preferences.Key<Int> = intPreferencesKey(AOD_CLOCK_FACE_KEY)
    val aodClockFace: StateFlow<Int?> =
        aodClockFaceKey.asStateFlow(default = DEFAULT_AOD_CLOCK_FACE)

    private val aodClockFontKey: Preferences.Key<Int> = intPreferencesKey(AOD_CLOCK_FONT_KEY)
    val aodClockFont: StateFlow<Int?> =
        aodClockFontKey.asStateFlow(default = DEFAULT_AOD_CLOCK_FONT)

    private val aodClockColorKey: Preferences.Key<Int> = intPreferencesKey(AOD_CLOCK_COLOR_KEY)
    val aodClockColor: StateFlow<Int?> =
        aodClockColorKey.asStateFlow(default = DEFAULT_AOD_CLOCK_COLOR)

    private val aodClockSizePercentKey: Preferences.Key<Int> = intPreferencesKey(AOD_CLOCK_SIZE_PERCENT_KEY)
    val aodClockSizePercent: StateFlow<Int?> =
        aodClockSizePercentKey.asStateFlow(default = DEFAULT_AOD_CLOCK_SIZE_PERCENT)

    private val isAodLandscapeKey: Preferences.Key<Boolean> = booleanPreferencesKey(IS_AOD_LANDSCAPE_KEY)
    val isAodLandscape: StateFlow<Boolean?> =
        isAodLandscapeKey.asStateFlow(default = DEFAULT_IS_AOD_LANDSCAPE)

    private val aodDoubleTapActionKey: Preferences.Key<Int> = intPreferencesKey(AOD_DOUBLE_TAP_ACTION_KEY)
    val aodDoubleTapAction: StateFlow<Int?> =
        aodDoubleTapActionKey.asStateFlow(default = DEFAULT_AOD_DOUBLE_TAP_ACTION)

    private val aodSwipeUpActionKey: Preferences.Key<Int> = intPreferencesKey(AOD_SWIPE_UP_ACTION_KEY)
    val aodSwipeUpAction: StateFlow<Int?> =
        aodSwipeUpActionKey.asStateFlow(default = DEFAULT_AOD_SWIPE_UP_ACTION)

    private val aodSwipeDownActionKey: Preferences.Key<Int> = intPreferencesKey(AOD_SWIPE_DOWN_ACTION_KEY)
    val aodSwipeDownAction: StateFlow<Int?> =
        aodSwipeDownActionKey.asStateFlow(default = DEFAULT_AOD_SWIPE_DOWN_ACTION)

    private val aodVolumeUpActionKey: Preferences.Key<Int> = intPreferencesKey(AOD_VOLUME_UP_ACTION_KEY)
    val aodVolumeUpAction: StateFlow<Int?> =
        aodVolumeUpActionKey.asStateFlow(default = DEFAULT_AOD_VOLUME_UP_ACTION)

    private val aodVolumeDownActionKey: Preferences.Key<Int> = intPreferencesKey(AOD_VOLUME_DOWN_ACTION_KEY)
    val aodVolumeDownAction: StateFlow<Int?> =
        aodVolumeDownActionKey.asStateFlow(default = DEFAULT_AOD_VOLUME_DOWN_ACTION)

    private val aodBackActionKey: Preferences.Key<Int> = intPreferencesKey(AOD_BACK_ACTION_KEY)
    val aodBackAction: StateFlow<Int?> =
        aodBackActionKey.asStateFlow(default = DEFAULT_AOD_BACK_ACTION)

    private val isAodAutoDimEnabledKey: Preferences.Key<Boolean> = booleanPreferencesKey(IS_AOD_AUTO_DIM_ENABLED_KEY)
    val isAodAutoDimEnabled: StateFlow<Boolean?> =
        isAodAutoDimEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_AUTO_DIM_ENABLED)

    private val isAodRaiseToWakeEnabledKey: Preferences.Key<Boolean> = booleanPreferencesKey(IS_AOD_RAISE_TO_WAKE_ENABLED_KEY)
    val isAodRaiseToWakeEnabled: StateFlow<Boolean?> =
        isAodRaiseToWakeEnabledKey.asStateFlow(default = DEFAULT_IS_AOD_RAISE_TO_WAKE_ENABLED)

    private val aodLastWakeKey: Preferences.Key<Int> = intPreferencesKey(AOD_LAST_WAKE_KEY)
    val aodLastWake: StateFlow<Int?> =
        aodLastWakeKey.asStateFlow(default = DEFAULT_AOD_LAST_WAKE)

    private val isNotificationsAskedKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_NOTIFICATIONS_ASKED_KEY)
    val isNotificationsAsked: StateFlow<Boolean?> =
        isNotificationsAskedKey.asStateFlow(default = DEFAULT_IS_NOTIFICATIONS_ASKED)

    fun saveSelectedLangCode(value: String) {
        edit { prefs -> prefs[selectedLangCodeKey] = value }
    }

    fun saveIsFirstOpen(value: Boolean) {
        edit { prefs -> prefs[isFirstOpenKey] = value }
    }

    fun saveIsAodEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodEnabledKey] = value }
    }

    fun saveIsAodCustomBrightness(value: Boolean) {
        edit { prefs -> prefs[isAodCustomBrightnessKey] = value }
    }

    fun saveAodBrightnessPercent(value: Int) {
        edit { prefs -> prefs[aodBrightnessPercentKey] = value }
    }

    fun saveIsAodProximityEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodProximityEnabledKey] = value }
    }

    fun saveAodTimeoutMinutes(value: Int) {
        edit { prefs -> prefs[aodTimeoutMinutesKey] = value }
    }

    fun saveAodMinBattery(value: Int) {
        edit { prefs -> prefs[aodMinBatteryKey] = value }
    }

    fun saveAodChargingRule(value: Int) {
        edit { prefs -> prefs[aodChargingRuleKey] = value }
    }

    fun saveIsAodScheduleEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodScheduleEnabledKey] = value }
    }

    fun saveAodScheduleStartMinute(value: Int) {
        edit { prefs -> prefs[aodScheduleStartMinuteKey] = value }
    }

    fun saveAodScheduleEndMinute(value: Int) {
        edit { prefs -> prefs[aodScheduleEndMinuteKey] = value }
    }

    fun saveIsAodNotificationIconsEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodNotificationIconsEnabledKey] = value }
    }

    fun saveIsAodEdgeGlowEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodEdgeGlowEnabledKey] = value }
    }

    fun saveIsAodMediaControlsEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodMediaControlsEnabledKey] = value }
    }

    fun saveAodClockFace(value: Int) {
        edit { prefs -> prefs[aodClockFaceKey] = value }
    }

    fun saveAodClockFont(value: Int) {
        edit { prefs -> prefs[aodClockFontKey] = value }
    }

    fun saveAodClockColor(value: Int) {
        edit { prefs -> prefs[aodClockColorKey] = value }
    }

    fun saveAodClockSizePercent(value: Int) {
        edit { prefs -> prefs[aodClockSizePercentKey] = value }
    }

    fun saveIsAodLandscape(value: Boolean) {
        edit { prefs -> prefs[isAodLandscapeKey] = value }
    }

    fun saveAodDoubleTapAction(value: Int) {
        edit { prefs -> prefs[aodDoubleTapActionKey] = value }
    }

    fun saveAodSwipeUpAction(value: Int) {
        edit { prefs -> prefs[aodSwipeUpActionKey] = value }
    }

    fun saveAodSwipeDownAction(value: Int) {
        edit { prefs -> prefs[aodSwipeDownActionKey] = value }
    }

    fun saveAodVolumeUpAction(value: Int) {
        edit { prefs -> prefs[aodVolumeUpActionKey] = value }
    }

    fun saveAodVolumeDownAction(value: Int) {
        edit { prefs -> prefs[aodVolumeDownActionKey] = value }
    }

    fun saveAodBackAction(value: Int) {
        edit { prefs -> prefs[aodBackActionKey] = value }
    }

    fun saveIsAodAutoDimEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodAutoDimEnabledKey] = value }
    }

    fun saveIsAodRaiseToWakeEnabled(value: Boolean) {
        edit { prefs -> prefs[isAodRaiseToWakeEnabledKey] = value }
    }

    fun saveAodLastWake(value: Int) {
        edit { prefs -> prefs[aodLastWakeKey] = value }
    }

    fun saveIsNotificationsAsked(value: Boolean) {
        edit { prefs -> prefs[isNotificationsAskedKey] = value }
    }

    private fun <T : Any> Preferences.Key<T>.asStateFlow(default: T): StateFlow<T?> =
        app.prefs.data
            .map { prefs -> prefs[this] ?: default }
            .recoverCatching { throwable ->
                Timber.tag(tag)
                    .e("DataStore read $name failed, falling back to $default: ${throwable.stackTraceToString()}")
                emit(default)
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    private fun edit(transform: (MutablePreferences) -> Unit) {
        scope.launch {
            withContextCatching(
                action = { app.prefs.edit(transform) },
                catch = { throwable ->
                    Timber.tag(tag).e("DataStore edit failed: ${throwable.stackTraceToString()}")
                },
            )
        }
    }

    companion object {
        private const val DATA_STORE_NAME = "custom_aod_prefs"
        private const val SELECTED_LANG_CODE_KEY = "selected_lang_code"
        private const val IS_FIRST_OPEN_KEY = "is_first_open"
        private const val IS_AOD_ENABLED_KEY = "is_aod_enabled"
        private const val IS_AOD_CUSTOM_BRIGHTNESS_KEY = "is_aod_custom_brightness"
        private const val AOD_BRIGHTNESS_PERCENT_KEY = "aod_brightness_percent"
        private const val IS_AOD_PROXIMITY_ENABLED_KEY = "is_aod_proximity_enabled"
        private const val AOD_TIMEOUT_MINUTES_KEY = "aod_timeout_minutes"
        private const val AOD_MIN_BATTERY_KEY = "aod_min_battery"
        private const val AOD_CHARGING_RULE_KEY = "aod_charging_rule"
        private const val IS_AOD_SCHEDULE_ENABLED_KEY = "is_aod_schedule_enabled"
        private const val AOD_SCHEDULE_START_MINUTE_KEY = "aod_schedule_start_minute"
        private const val AOD_SCHEDULE_END_MINUTE_KEY = "aod_schedule_end_minute"
        private const val IS_AOD_NOTIFICATION_ICONS_ENABLED_KEY = "is_aod_notification_icons_enabled"
        private const val IS_AOD_EDGE_GLOW_ENABLED_KEY = "is_aod_edge_glow_enabled"
        private const val IS_AOD_MEDIA_CONTROLS_ENABLED_KEY = "is_aod_media_controls_enabled"
        private const val AOD_CLOCK_FACE_KEY = "aod_clock_face"
        private const val AOD_CLOCK_FONT_KEY = "aod_clock_font"
        private const val AOD_CLOCK_COLOR_KEY = "aod_clock_color"
        private const val AOD_CLOCK_SIZE_PERCENT_KEY = "aod_clock_size_percent"
        private const val IS_AOD_LANDSCAPE_KEY = "is_aod_landscape"
        private const val AOD_DOUBLE_TAP_ACTION_KEY = "aod_double_tap_action"
        private const val AOD_SWIPE_UP_ACTION_KEY = "aod_swipe_up_action"
        private const val AOD_SWIPE_DOWN_ACTION_KEY = "aod_swipe_down_action"
        private const val AOD_VOLUME_UP_ACTION_KEY = "aod_volume_up_action"
        private const val AOD_VOLUME_DOWN_ACTION_KEY = "aod_volume_down_action"
        private const val AOD_BACK_ACTION_KEY = "aod_back_action"
        private const val IS_AOD_AUTO_DIM_ENABLED_KEY = "is_aod_auto_dim_enabled"
        private const val IS_AOD_RAISE_TO_WAKE_ENABLED_KEY = "is_aod_raise_to_wake_enabled"
        private const val AOD_LAST_WAKE_KEY = "aod_last_wake"
        private const val IS_NOTIFICATIONS_ASKED_KEY = "is_notifications_asked"
        private const val DEFAULT_SELECTED_LANG_CODE = "en"
        private const val DEFAULT_IS_FIRST_OPEN = true
        const val DEFAULT_IS_AOD_ENABLED = true
        const val DEFAULT_IS_AOD_CUSTOM_BRIGHTNESS = true
        const val DEFAULT_AOD_BRIGHTNESS_PERCENT = 1
        const val DEFAULT_IS_AOD_PROXIMITY_ENABLED = true
        const val DEFAULT_AOD_TIMEOUT_MINUTES = 0
        const val DEFAULT_AOD_MIN_BATTERY = 15
        private const val DEFAULT_AOD_CHARGING_RULE = 0
        const val DEFAULT_IS_AOD_SCHEDULE_ENABLED = true
        const val DEFAULT_AOD_SCHEDULE_START_MINUTE = 7 * 60
        const val DEFAULT_AOD_SCHEDULE_END_MINUTE = 23 * 60
        const val DEFAULT_IS_AOD_NOTIFICATION_ICONS_ENABLED = true
        const val DEFAULT_IS_AOD_EDGE_GLOW_ENABLED = true
        const val DEFAULT_IS_AOD_MEDIA_CONTROLS_ENABLED = true
        private const val DEFAULT_AOD_CLOCK_FACE = 0
        private const val DEFAULT_AOD_CLOCK_FONT = 0
        private const val DEFAULT_AOD_CLOCK_COLOR = 0
        const val DEFAULT_AOD_CLOCK_SIZE_PERCENT = 100
        const val DEFAULT_IS_AOD_LANDSCAPE = false
        const val DEFAULT_AOD_DOUBLE_TAP_ACTION = 1
        const val DEFAULT_AOD_SWIPE_UP_ACTION = 0
        const val DEFAULT_AOD_SWIPE_DOWN_ACTION = 0
        const val DEFAULT_AOD_VOLUME_UP_ACTION = 0
        const val DEFAULT_AOD_VOLUME_DOWN_ACTION = 0
        const val DEFAULT_AOD_BACK_ACTION = 1
        const val DEFAULT_IS_AOD_AUTO_DIM_ENABLED = true
        const val DEFAULT_IS_AOD_RAISE_TO_WAKE_ENABLED = true
        private const val DEFAULT_AOD_LAST_WAKE = 0
        private const val DEFAULT_IS_NOTIFICATIONS_ASKED = false
    }
}
