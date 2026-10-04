package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager

@Immutable
data class AodRulesUiModel(
    val minBattery: Int = DataStoreManager.DEFAULT_AOD_MIN_BATTERY,
    val chargingRule: ChargingRuleValue = ChargingRuleValue.ALWAYS,
    val schedule: AodScheduleUiModel = AodScheduleUiModel(),
) {
    fun allows(batteryPercent: Int?, isCharging: Boolean, isPlugged: Boolean?, minuteOfDay: Int): Boolean =
        chargingRule.allows(isPlugged) &&
            schedule.isActiveAt(minuteOfDay) &&
            !isBatteryLow(batteryPercent, isCharging)

    // Đang sạc thì ngưỡng pin không áp dụng; không đọc được pin (null) thì không chặn AOD.
    private fun isBatteryLow(batteryPercent: Int?, isCharging: Boolean): Boolean =
        !isCharging && batteryPercent != null && batteryPercent in 0 until minBattery

    companion object {
        const val BATTERY_STEP_PERCENT = 5
        const val BATTERY_MAX_PERCENT = 50
    }
}

fun DataStoreManager.currentAodRules(): AodRulesUiModel = AodRulesUiModel(
    minBattery = aodMinBattery.value ?: DataStoreManager.DEFAULT_AOD_MIN_BATTERY,
    chargingRule = ChargingRuleValue.fromCode(aodChargingRule.value),
    schedule = AodScheduleUiModel(
        isEnabled = isAodScheduleEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_SCHEDULE_ENABLED,
        startMinute = aodScheduleStartMinute.value ?: DataStoreManager.DEFAULT_AOD_SCHEDULE_START_MINUTE,
        endMinute = aodScheduleEndMinute.value ?: DataStoreManager.DEFAULT_AOD_SCHEDULE_END_MINUTE,
    ),
)
