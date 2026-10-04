package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.domain.model.CalendarEvent
import java.time.Instant
import java.time.ZoneId

@Immutable
data class CalendarEventUiModel(
    val title: String,
    val beginMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
) {
    companion object {
        const val MAX_EVENTS = 2

        // Mốc 0 giờ ngày mai theo múi giờ của máy, tức hết hôm nay.
        fun endOfDayMillis(nowMillis: Long): Long {
            val zone = ZoneId.systemDefault()
            return Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        }
    }
}

fun CalendarEvent.toUiModel(): CalendarEventUiModel = CalendarEventUiModel(
    title = title,
    beginMillis = beginMillis,
    endMillis = endMillis,
    isAllDay = isAllDay,
)
