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

    private val isAodDimBrightnessKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(IS_AOD_DIM_BRIGHTNESS_KEY)
    val isAodDimBrightness: StateFlow<Boolean?> =
        isAodDimBrightnessKey.asStateFlow(default = DEFAULT_IS_AOD_DIM_BRIGHTNESS)

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

    fun saveIsAodDimBrightness(value: Boolean) {
        edit { prefs -> prefs[isAodDimBrightnessKey] = value }
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
        private const val IS_AOD_DIM_BRIGHTNESS_KEY = "is_aod_dim_brightness"
        private const val IS_AOD_PROXIMITY_ENABLED_KEY = "is_aod_proximity_enabled"
        private const val AOD_TIMEOUT_MINUTES_KEY = "aod_timeout_minutes"
        private const val AOD_MIN_BATTERY_KEY = "aod_min_battery"
        private const val AOD_LAST_WAKE_KEY = "aod_last_wake"
        private const val IS_NOTIFICATIONS_ASKED_KEY = "is_notifications_asked"
        private const val DEFAULT_SELECTED_LANG_CODE = "en"
        private const val DEFAULT_IS_FIRST_OPEN = true
        const val DEFAULT_IS_AOD_ENABLED = true
        const val DEFAULT_IS_AOD_DIM_BRIGHTNESS = true
        const val DEFAULT_IS_AOD_PROXIMITY_ENABLED = true
        const val DEFAULT_AOD_TIMEOUT_MINUTES = 0
        const val DEFAULT_AOD_MIN_BATTERY = 15
        private const val DEFAULT_AOD_LAST_WAKE = 0
        private const val DEFAULT_IS_NOTIFICATIONS_ASKED = false
    }
}
