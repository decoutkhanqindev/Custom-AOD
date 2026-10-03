package com.decoutkhanqindev.custom_aod.presentation.aod

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.data.device.audio.AudioStateManager
import com.decoutkhanqindev.custom_aod.data.device.battery.BatteryStateManager
import com.decoutkhanqindev.custom_aod.data.device.screen.ScreenStateManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.presentation.MainActivity
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.utils.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

// Mở AodActivity lúc màn hình còn tắt (nên nó che màn hình khoá trước khi màn hình sáng); foreground service là thứ giữ được receiver SCREEN_OFF mà không cần quyền đặc biệt.
class AodService : Service(), Tag {

    private val dataStoreManager: DataStoreManager by inject()
    private val languageManager: LanguageManager by inject()
    private val screenStateManager: ScreenStateManager by inject()
    private val audioStateManager: AudioStateManager by inject()
    private val batteryStateManager: BatteryStateManager by inject()
    private val session: AodSession by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var launchCheckJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            screenStateManager.events.collect { action ->
                when (action) {
                    Intent.ACTION_SCREEN_OFF -> onScreenOff()
                    Intent.ACTION_USER_PRESENT -> onUserPresent()
                }
            }
        }
        scope.launch {
            dataStoreManager.selectedLangCode
                .filterNotNull()
                .distinctUntilChanged()
                .collect { startInForeground() }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Thông báo đăng lúc chưa được phép thì bị ẩn: đăng lại mỗi lần start để nó hiện ngay khi user cho phép.
        startInForeground()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        session.finish()
        super.onDestroy()
    }

    private fun onScreenOff() {
        launchCheckJob?.cancel()
        val wasShowing = session.isShowing
        val wasCovered = session.isCovered
        val isSleepRequested = session.consumeSleepRequest()
        session.finish()
        when {
            // AOD đã chuyển sang đen và màn hình hết giờ chờ theo chủ ý: giữ tắt.
            isSleepRequested -> Unit
            // Trong túi, ROM có thể tự tắt màn hình: không bao giờ bật lên ở đó.
            wasCovered -> Unit
            // AOD giữ màn hình sáng nên lần tắt này là nút nguồn (app không bắt được phím): về màn hình khoá như AOD thật.
            wasShowing -> screenStateManager.wakeUp(holdMillis = WAKE_HOLD_MILLIS)
            shouldEnter() -> launch()
        }
    }

    private fun onUserPresent() {
        session.clearSleepRequest()
        if (!session.isShowing) return

        // Không có PIN/hình vẽ thì không có keyguard để mở: USER_PRESENT đến ngay sau lần bật màn hình của chính app, lần đó không được đóng AOD.
        val isJustShown = SystemClock.elapsedRealtime() - session.shownAt < USER_PRESENT_GRACE_MILLIS
        if (isJustShown && !screenStateManager.isDeviceSecure) return

        session.finish()
    }

    private fun shouldEnter(): Boolean {
        if (dataStoreManager.isAodEnabled.value != true) return false
        // SCREEN_OFF đến trễ: nút nguồn có thể đã bật lại màn hình, lúc đó user muốn màn hình khoá chứ không phải AOD.
        if (screenStateManager.isInteractive) return false
        // Đang có chuông, cuộc gọi hoặc báo thức: AOD sẽ bị mở đè lên màn hình đó.
        if (audioStateManager.isBusyNow()) return false

        val minBattery = dataStoreManager.aodMinBattery.value ?: DataStoreManager.DEFAULT_AOD_MIN_BATTERY
        val percent = batteryStateManager.readLevelPercent() ?: return true
        return batteryStateManager.readIsCharging() == true || percent !in 0 until minBattery
    }

    // Mở Activity từ nền được là nhờ user đã cấp "Hiển thị trên ứng dụng khác".
    private fun launch() {
        val intent = Intent(this, AodActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NO_ANIMATION or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION,
        )
        try {
            startActivity(intent)
        } catch (e: RuntimeException) {
            Timber.tag(tag).e("Could not start the AOD: ${e.stackTraceToString()}")
        }
        launchCheckJob?.cancel()
        launchCheckJob = scope.launch {
            delay(LAUNCH_CHECK_MILLIS)
            checkLaunch()
        }
    }

    private fun checkLaunch() {
        if (session.isShowing && screenStateManager.isInteractive) {
            dataStoreManager.saveAodLastWake(WakeResultValue.OK.code)
        } else {
            // Bị chặn thì không có Activity; trên Xiaomi thiếu "Hiển thị trên màn hình khoá" thì nó nằm sau màn hình khoá và màn hình vẫn tắt: đóng đi để nó không hiện ra sau lần mở khoá kế tiếp.
            dataStoreManager.saveAodLastWake(WakeResultValue.FAILED.code)
            session.finish()
        }
    }

    private fun startInForeground() {
        val languageCode = LanguageValue.fromCode(dataStoreManager.selectedLangCode.value).code
        val localizedResources = languageManager.resourcesFor(languageManager.configurationFor(languageCode))
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                localizedResources.getString(R.string.channel_service),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) },
        )
        val openApp = PendingIntent.getActivity(
            this,
            OPEN_APP_REQUEST_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_aod)
            .setContentTitle(localizedResources.getString(R.string.notif_service_title))
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "aod_service"
        private const val NOTIFICATION_ID = 1
        private const val OPEN_APP_REQUEST_CODE = 1
        private const val LAUNCH_CHECK_MILLIS = 2_000L
        private const val WAKE_HOLD_MILLIS = 1_000L
        private const val USER_PRESENT_GRACE_MILLIS = 1_500L

        fun start(context: Context) {
            try {
                context.startForegroundService(Intent(context, AodService::class.java))
            } catch (e: IllegalStateException) {
                // Android 12+ cấm start foreground service từ nền; boot, cập nhật app và màn hình của app đều được miễn nên đây chỉ là chốt an toàn.
                Timber.tag(AodService::class.java.simpleName)
                    .e("Could not start the AOD service: ${e.stackTraceToString()}")
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AodService::class.java))
        }
    }
}
