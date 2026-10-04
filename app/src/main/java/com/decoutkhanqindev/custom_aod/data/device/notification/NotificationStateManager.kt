package com.decoutkhanqindev.custom_aod.data.device.notification

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.os.Build
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.NotificationListenerService.RankingMap
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.BundleCompat
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.withContextCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

// App không tự đăng ký nhận thông báo được: chỉ listener do hệ thống bind (khi user đã cấp "Truy cập thông báo") nhận, nên listener đẩy dữ liệu vào đây.
class NotificationStateManager(
    private val app: Application,
) : Tag {
    // Một luồng nền xử lý lần lượt từng callback: giữ đúng thứ tự, và nạp icon từ resource của app khác không chặn main thread.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val iconSizePx = (ICON_SIZE_DP * app.resources.displayMetrics.density).roundToInt()
    private val iconCache = HashMap<String, Bitmap>()
    private var shownKeys = emptySet<String>()

    // Nóng sẵn vì listener được bind suốt khi đã có quyền: khung đầu tiên của AOD đọc giá trị hiện tại là đủ. Mới nhất trước.
    private val _notifications = MutableStateFlow<List<ActiveNotification>>(emptyList())
    val notifications: StateFlow<List<ActiveNotification>> = _notifications.asStateFlow()

    // Sự kiện, không phải state: mỗi thông báo mới chỉ báo một lần; không ai collect (AOD không hiện) thì bỏ.
    private val _alerts = MutableSharedFlow<ActiveNotification>(
        extraBufferCapacity = ALERT_BUFFER_SIZE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val alerts: SharedFlow<ActiveNotification> = _alerts.asSharedFlow()

    private val _mediaSessionToken = MutableStateFlow<MediaSession.Token?>(null)
    val mediaSessionToken: StateFlow<MediaSession.Token?> = _mediaSessionToken.asStateFlow()

    fun onListenerConnected(active: List<StatusBarNotification>, rankingMap: RankingMap) {
        update(active = active, rankingMap = rankingMap, posted = null)
    }

    fun onNotificationPosted(
        posted: StatusBarNotification,
        active: List<StatusBarNotification>,
        rankingMap: RankingMap,
    ) {
        update(active = active, rankingMap = rankingMap, posted = posted)
    }

    fun onNotificationsChanged(active: List<StatusBarNotification>, rankingMap: RankingMap) {
        update(active = active, rankingMap = rankingMap, posted = null)
    }

    // Mất quyền hoặc bị huỷ bind: không còn biết thông báo nào đang có.
    fun onListenerDisconnected() {
        scope.launch {
            shownKeys = emptySet()
            iconCache.clear()
            _notifications.value = emptyList()
            _mediaSessionToken.value = null
        }
    }

    private fun update(
        active: List<StatusBarNotification>,
        rankingMap: RankingMap,
        posted: StatusBarNotification?,
    ) {
        scope.launch {
            withContextCatching(
                action = {
                    val ranking = Ranking()
                    val shown = active
                        .filter { sbn -> rankingMap.getRanking(sbn.key, ranking) && sbn.isShownOnAmbient(ranking) }
                        .sortedByDescending { sbn -> sbn.postTime }
                        .mapNotNull { sbn -> sbn.toActiveNotification() }
                    val alert = posted?.let { sbn ->
                        shown.find { it.key == sbn.key }
                            ?.takeIf { sbn.isAlerting(rankingMap = rankingMap, isNew = sbn.key !in shownKeys) }
                    }
                    val shownPackages = shown.mapTo(HashSet()) { it.packageName }
                    iconCache.keys.retainAll { cacheKey -> cacheKey.substringBefore(ICON_CACHE_SEPARATOR) in shownPackages }
                    shownKeys = shown.mapTo(HashSet()) { it.key }
                    _notifications.value = shown
                    _mediaSessionToken.value = active.latestMediaSessionToken(rankingMap)
                    if (alert != null) _alerts.tryEmit(alert)
                },
                catch = { e -> Timber.tag(tag).e("Notification update failed: ${e.stackTraceToString()}") },
            )
        }
    }

    // Như màn hình chờ của hệ thống: bỏ thông báo thường trực, tóm tắt nhóm, im lặng, bị Không làm phiền ẩn khỏi màn hình chờ, của chính app, và thông báo nhạc (đã có điều khiển nhạc).
    private fun StatusBarNotification.isShownOnAmbient(ranking: Ranking): Boolean =
        packageName != app.packageName &&
            !isOngoing &&
            notification.flags and HIDDEN_FLAGS == 0 &&
            notification.mediaSessionToken == null &&
            isVisibleOnLockScreen(ranking) &&
            ranking.importance >= NotificationManager.IMPORTANCE_DEFAULT &&
            ranking.suppressedVisualEffects and NotificationManager.Policy.SUPPRESSED_EFFECT_AMBIENT == 0

    // App hoặc user đã ẩn thông báo này trên màn hình khoá (VISIBILITY_SECRET; tuỳ chỉnh theo kênh chỉ đọc được từ Android 12), hoặc app đang bị tạm ngưng.
    private fun StatusBarNotification.isVisibleOnLockScreen(ranking: Ranking): Boolean =
        notification.visibility != Notification.VISIBILITY_SECRET &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ranking.lockscreenVisibilityOverride != Notification.VISIBILITY_SECRET) &&
            !ranking.isSuspended

    // Thông báo mới, hoặc cập nhật không đặt "chỉ báo một lần" (vd tin nhắn mới trong cùng cuộc trò chuyện); bị Không làm phiền chặn thì không báo.
    private fun StatusBarNotification.isAlerting(rankingMap: RankingMap, isNew: Boolean): Boolean {
        val ranking = Ranking()
        return rankingMap.getRanking(key, ranking) &&
            ranking.matchesInterruptionFilter() &&
            (isNew || notification.flags and Notification.FLAG_ONLY_ALERT_ONCE == 0)
    }

    // Thông báo nhạc mới nhất, cách trình phát nhạc trên màn hình khoá của hệ thống chọn phiên nhạc.
    private fun List<StatusBarNotification>.latestMediaSessionToken(rankingMap: RankingMap): MediaSession.Token? {
        val ranking = Ranking()
        return filter { sbn ->
            sbn.notification.mediaSessionToken != null &&
                rankingMap.getRanking(sbn.key, ranking) &&
                sbn.isVisibleOnLockScreen(ranking)
        }
            .maxByOrNull { sbn -> sbn.postTime }
            ?.notification
            ?.mediaSessionToken
    }

    private val Notification.mediaSessionToken: MediaSession.Token?
        get() = BundleCompat.getParcelable(extras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)

    private fun StatusBarNotification.toActiveNotification(): ActiveNotification? {
        val icon = loadSmallIcon() ?: return null
        return ActiveNotification(
            key = key,
            packageName = packageName,
            icon = icon,
            color = notification.color,
            postTime = postTime,
        )
    }

    // Nạp sẵn ở đây để AOD vẽ ngay khung đầu tiên; cache theo (package, resource) vì app thường đăng lại cùng thông báo với cùng icon.
    private fun StatusBarNotification.loadSmallIcon(): Bitmap? {
        val icon = notification.smallIcon ?: return null
        val cacheKey = if (icon.type == Icon.TYPE_RESOURCE) {
            "$packageName$ICON_CACHE_SEPARATOR${icon.resPackage}:${icon.resId}"
        } else {
            null
        }
        cacheKey?.let { iconCache[it] }?.let { return it }
        return icon.loadDrawable(app)
            ?.toBitmap(width = iconSizePx, height = iconSizePx)
            ?.also { bitmap -> cacheKey?.let { iconCache[it] = bitmap } }
    }

    companion object {
        private const val ICON_SIZE_DP = 24
        private const val ICON_CACHE_SEPARATOR = '/'
        private const val ALERT_BUFFER_SIZE = 8
        private const val HIDDEN_FLAGS = Notification.FLAG_FOREGROUND_SERVICE or Notification.FLAG_GROUP_SUMMARY
    }
}
