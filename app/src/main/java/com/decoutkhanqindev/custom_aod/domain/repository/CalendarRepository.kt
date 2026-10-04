package com.decoutkhanqindev.custom_aod.domain.repository

import com.decoutkhanqindev.custom_aod.domain.model.CalendarEvent

interface CalendarRepository {
    // Sự kiện của các lịch đang hiện, có phần nằm trong [fromMillis, toMillis], sớm nhất trước; bỏ sự kiện user đã từ chối. Đọc lỗi thì rỗng.
    suspend fun getEvents(fromMillis: Long, toMillis: Long, limit: Int): List<CalendarEvent>
}
