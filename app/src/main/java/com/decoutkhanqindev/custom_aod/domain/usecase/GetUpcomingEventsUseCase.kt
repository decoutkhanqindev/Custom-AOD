package com.decoutkhanqindev.custom_aod.domain.usecase

import com.decoutkhanqindev.custom_aod.domain.model.CalendarEvent
import com.decoutkhanqindev.custom_aod.domain.repository.CalendarRepository
import com.decoutkhanqindev.custom_aod.utils.suspendRunCatching

// Sự kiện còn lại trong hôm nay, kể cả đang diễn ra: từ lúc này tới hết ngày.
class GetUpcomingEventsUseCase(private val repository: CalendarRepository) {
    suspend operator fun invoke(nowMillis: Long, endOfDayMillis: Long, limit: Int): Result<List<CalendarEvent>> =
        suspendRunCatching { repository.getEvents(fromMillis = nowMillis, toMillis = endOfDayMillis, limit = limit) }
}
