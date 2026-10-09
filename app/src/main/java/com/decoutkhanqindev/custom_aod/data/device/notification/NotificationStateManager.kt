package com.decoutkhanqindev.custom_aod.data.device.notification

import android.app.Application
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.os.Build
import android.provider.Settings
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
    private val keyguardManager = app.getSystemService(KeyguardManager::class.java)
    private val devicePolicyManager: DevicePolicyManager? = app.getSystemService(DevicePolicyManager::class.java)
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
                block = {
                    val lockScreen = readLockScreenPolicy()
                    val shown = active
                        .mapNotNull { sbn ->
                            val ranking = Ranking()
                            (sbn to ranking).takeIf {
                                rankingMap.getRanking(sbn.key, ranking) && sbn.isShownOnAmbient(ranking)
                            }
                        }
                        .sortedByDescending { (sbn, _) -> sbn.postTime }
                        .mapNotNull { (sbn, ranking) ->
                            sbn.toActiveNotification(content = sbn.lockScreenContent(ranking, lockScreen))
                        }
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

    // Đọc lại mỗi lần cập nhật vì user đổi cài đặt màn hình khoá lúc nào cũng được. Quản lý thiết bị cấm hiện nội dung thì coi như tắt "nội dung nhạy cảm".
    private fun readLockScreenPolicy(): LockScreenPolicy {
        val disabledFeatures = devicePolicyManager?.getKeyguardDisabledFeatures(null) ?: 0
        val isRedactionForced = disabledFeatures and DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS != 0
        return LockScreenPolicy(
            isContentShown = isLockScreenSettingOn(LOCK_SCREEN_SHOW_NOTIFICATIONS),
            isSecure = keyguardManager.isDeviceSecure,
            isPrivateContentShown = !isRedactionForced && isLockScreenSettingOn(LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS),
        )
    }

    // Hai cài đặt này không có hằng số public nhưng app vẫn đọc được; chưa đặt = mặc định của hệ thống (bật). ROM chặn đọc thì coi như tắt cho kín đáo.
    private fun isLockScreenSettingOn(name: String): Boolean =
        try {
            Settings.Secure.getInt(app.contentResolver, name, SETTING_ON) != SETTING_OFF
        } catch (e: SecurityException) {
            Timber.tag(tag).w("Could not read $name: ${e.message}")
            false
        }

    // Như màn hình khoá của hệ thống: tắt hiện thông báo thì che hết; máy có khoá bảo mật thì che thông báo VISIBILITY_PRIVATE khi tắt "nội dung nhạy cảm", và che kênh user đặt "ẩn nội dung" (đọc được từ Android 12). Bị che thì dùng bản công khai app tự soạn, không có thì null.
    private fun StatusBarNotification.lockScreenContent(ranking: Ranking, policy: LockScreenPolicy): NotificationContent? {
        if (!policy.isContentShown) return null
        val isChannelPrivate = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ranking.lockscreenVisibilityOverride == Notification.VISIBILITY_PRIVATE
        val isAppPrivate = notification.visibility == Notification.VISIBILITY_PRIVATE && !policy.isPrivateContentShown
        val isRedacted = policy.isSecure && (isChannelPrivate || isAppPrivate)
        val source = if (isRedacted) notification.publicVersion else notification
        return source?.content()
    }

    // Tiêu đề và một dòng nội dung như thông báo thu gọn; app chỉ đặt chữ dài (BigTextStyle) thì lấy chữ đó.
    private fun Notification.content(): NotificationContent = NotificationContent(
        title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty(),
        text = (extras.getCharSequence(Notification.EXTRA_TEXT) ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
            ?.toString()
            ?.trim()
            .orEmpty(),
    )

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

    private fun StatusBarNotification.toActiveNotification(content: NotificationContent?): ActiveNotification? {
        val icon = loadSmallIcon() ?: return null
        return ActiveNotification(
            key = key,
            packageName = packageName,
            icon = icon,
            color = notification.color,
            postTime = postTime,
            content = content,
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
        private const val LOCK_SCREEN_SHOW_NOTIFICATIONS = "lock_screen_show_notifications"
        private const val LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS = "lock_screen_allow_private_notifications"
        private const val SETTING_ON = 1
        private const val SETTING_OFF = 0
    }

    // Màn hình khoá của hệ thống đang cho hiện tới đâu.
    private class LockScreenPolicy(
        val isContentShown: Boolean,
        val isSecure: Boolean,
        val isPrivateContentShown: Boolean,
    )
}
