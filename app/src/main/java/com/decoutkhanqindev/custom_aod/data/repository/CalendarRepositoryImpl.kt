package com.decoutkhanqindev.custom_aod.data.repository

import android.app.Application
import android.content.ContentUris
import android.provider.CalendarContract
import com.decoutkhanqindev.custom_aod.domain.model.CalendarEvent
import com.decoutkhanqindev.custom_aod.domain.repository.CalendarRepository
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.withContextCatching
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class CalendarRepositoryImpl(
    private val app: Application,
) : CalendarRepository, Tag {

    // Đọc lỗi (vd quyền READ_CALENDAR bị thu hồi sau khi đã bật) thì coi như hôm nay không có sự kiện: đồng hồ không giữ sự kiện cũ.
    override suspend fun getEvents(fromMillis: Long, toMillis: Long, limit: Int): List<CalendarEvent> =
        withContextCatching(
            context = Dispatchers.IO,
            block = { queryEvents(fromMillis = fromMillis, toMillis = toMillis, limit = limit) },
            catch = { e ->
                Timber.tag(tag).w("Could not read calendar events: ${e.message}")
                emptyList()
            },
        )

    private fun queryEvents(fromMillis: Long, toMillis: Long, limit: Int): List<CalendarEvent> {
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .also { builder ->
                ContentUris.appendId(builder, fromMillis)
                ContentUris.appendId(builder, toMillis)
            }
            .build()
        return app.contentResolver.query(uri, PROJECTION, SELECTION, arrayOf(fromMillis.toString()), SORT_ORDER)
            ?.use { cursor ->
                buildList {
                    while (size < limit && cursor.moveToNext()) {
                        val event = CalendarEvent(
                            title = cursor.getString(TITLE_INDEX).orEmpty(),
                            beginMillis = cursor.getLong(BEGIN_INDEX),
                            endMillis = cursor.getLong(END_INDEX),
                            isAllDay = cursor.getInt(ALL_DAY_INDEX) == 1,
                        )
                        if (event.isOnLocalDayOf(fromMillis)) add(event)
                    }
                }
            }
            .orEmpty()
    }

    // Sự kiện cả ngày lưu theo nửa đêm UTC: so theo ngày ghi trên lịch với ngày ở múi giờ của máy, không so theo mốc thời gian.
    private fun CalendarEvent.isOnLocalDayOf(nowMillis: Long): Boolean {
        if (!isAllDay) return true
        val today = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val firstDay = Instant.ofEpochMilli(beginMillis).atZone(ZoneOffset.UTC).toLocalDate()
        val endDayExclusive = Instant.ofEpochMilli(endMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return !today.isBefore(firstDay) && today.isBefore(endDayExclusive)
    }

    companion object {
        private val PROJECTION = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
        )
        private const val TITLE_INDEX = 0
        private const val BEGIN_INDEX = 1
        private const val END_INDEX = 2
        private const val ALL_DAY_INDEX = 3

        // Chỉ lịch đang hiện trong ứng dụng Lịch, bỏ sự kiện user đã từ chối và sự kiện đã kết thúc.
        private const val SELECTION = "${CalendarContract.Instances.VISIBLE} = 1" +
            " AND ${CalendarContract.Instances.SELF_ATTENDEE_STATUS} != ${CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED}" +
            " AND ${CalendarContract.Instances.END} > ?"
        private const val SORT_ORDER = "${CalendarContract.Instances.ALL_DAY} DESC, ${CalendarContract.Instances.BEGIN} ASC"
    }
}
