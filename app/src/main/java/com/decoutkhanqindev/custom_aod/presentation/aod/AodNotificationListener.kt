package com.decoutkhanqindev.custom_aod.presentation.aod

import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.decoutkhanqindev.custom_aod.data.device.notification.NotificationStateManager
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.openNotificationListenerSettings
import org.koin.android.ext.android.inject
import timber.log.Timber

// Hệ thống chỉ bind listener này khi user đã cấp "Truy cập thông báo"; callback chạy trên main thread và chỉ chuyển dữ liệu sang NotificationStateManager.
class AodNotificationListener : NotificationListenerService(), Tag {

    private val notificationStateManager: NotificationStateManager by inject()

    override fun onListenerConnected() {
        super.onListenerConnected()
        val active = readActiveNotifications() ?: return
        val rankingMap = currentRanking ?: return
        notificationStateManager.onListenerConnected(active, rankingMap)
    }

    override fun onListenerDisconnected() {
        notificationStateManager.onListenerDisconnected()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        val active = readActiveNotifications() ?: return
        notificationStateManager.onNotificationPosted(sbn, active, rankingMap)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) {
        val active = readActiveNotifications() ?: return
        notificationStateManager.onNotificationsChanged(active, rankingMap)
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap) {
        val active = readActiveNotifications() ?: return
        notificationStateManager.onNotificationsChanged(active, rankingMap)
    }

    // Đọc đúng lúc listener vừa bị huỷ bind thì hệ thống ném SecurityException: bỏ qua lần cập nhật đó.
    private fun readActiveNotifications(): List<StatusBarNotification>? = try {
        activeNotifications.orEmpty().toList()
    } catch (e: SecurityException) {
        Timber.tag(tag).e("Could not read active notifications: ${e.stackTraceToString()}")
        null
    }

    companion object {
        fun openAccessSettings(context: Context, isGranted: () -> Boolean) {
            context.openNotificationListenerSettings(
                listener = ComponentName(context, AodNotificationListener::class.java),
                isGranted = isGranted,
            )
        }
    }
}
