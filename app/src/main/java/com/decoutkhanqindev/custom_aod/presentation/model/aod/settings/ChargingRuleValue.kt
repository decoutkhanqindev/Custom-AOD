package com.decoutkhanqindev.custom_aod.presentation.model.aod.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class ChargingRuleValue(
    val code: Int,
    @param:StringRes val labelRes: Int,
) {
    ALWAYS(code = 0, labelRes = R.string.charging_rule_always),
    PLUGGED(code = 1, labelRes = R.string.charging_rule_plugged),
    ON_BATTERY(code = 2, labelRes = R.string.charging_rule_on_battery);

    // Không đọc được trạng thái cắm nguồn (null) thì không chặn AOD.
    fun allows(isPlugged: Boolean?): Boolean = when (this) {
        ALWAYS -> true
        PLUGGED -> isPlugged != false
        ON_BATTERY -> isPlugged != true
    }

    companion object {
        fun fromCode(code: Int?): ChargingRuleValue = entries.find { it.code == code } ?: ALWAYS
    }
}
