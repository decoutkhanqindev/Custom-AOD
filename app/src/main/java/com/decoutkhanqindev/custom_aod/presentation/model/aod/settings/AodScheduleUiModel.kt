package com.decoutkhanqindev.custom_aod.presentation.model.aod.settings

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@Immutable
data class AodScheduleUiModel(
    val isEnabled: Boolean = DataStoreManager.DEFAULT_IS_AOD_SCHEDULE_ENABLED,
    val startMinute: Int = DataStoreManager.DEFAULT_AOD_SCHEDULE_START_MINUTE,
    val endMinute: Int = DataStoreManager.DEFAULT_AOD_SCHEDULE_END_MINUTE,
) {
    // Giờ bắt đầu sau giờ kết thúc là khung giờ qua nửa đêm (vd 22:00–06:00); bắt đầu trùng kết thúc là cả ngày.
    fun isActiveAt(minuteOfDay: Int): Boolean = when {
        !isEnabled || startMinute == endMinute -> true
        startMinute < endMinute -> minuteOfDay in startMinute until endMinute
        else -> minuteOfDay >= startMinute || minuteOfDay < endMinute
    }

    fun minuteOf(time: ScheduleTimeValue): Int = when (time) {
        ScheduleTimeValue.START -> startMinute
        ScheduleTimeValue.END -> endMinute
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60

        fun timeOf(minuteOfDay: Int): LocalTime =
            LocalTime.ofSecondOfDay(minuteOfDay.toLong() * SECONDS_PER_MINUTE)

        fun minuteOfDay(time: LocalTime): Int = time.toSecondOfDay() / SECONDS_PER_MINUTE

        fun minuteOfDay(epochMillis: Long): Int =
            minuteOfDay(
                Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
            )
    }
}
