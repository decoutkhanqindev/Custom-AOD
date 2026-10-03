package com.decoutkhanqindev.custom_aod.data.device.screen

import android.app.Application
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.recoverCatching
import com.decoutkhanqindev.custom_aod.utils.registerSystemReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber

class ScreenStateManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val powerManager = app.getSystemService(PowerManager::class.java)
    private val keyguardManager = app.getSystemService(KeyguardManager::class.java)

    // Sự kiện, không phải state: StateFlow sẽ gộp mất một lần tắt rồi bật lại ngay. SCREEN_OFF chỉ tới receiver đăng ký lúc chạy.
    val events: SharedFlow<String> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.action?.let { trySend(it) }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        app.registerSystemReceiver(receiver, filter)
        awaitClose { app.unregisterReceiver(receiver) }
    }
        .recoverCatching { throwable ->
            Timber.tag(tag).e("events have error: ${throwable.stackTraceToString()}")
        }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
        )

    val isInteractive: Boolean get() = powerManager.isInteractive

    val isDeviceSecure: Boolean get() = keyguardManager.isDeviceSecure

    @Suppress("DEPRECATION") // SCREEN_BRIGHT_WAKE_LOCK: không có Activity thì không còn cách nào khác để bật màn hình
    fun wakeUp(holdMillis: Long) {
        powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE, // sau đó màn hình khoá tự tắt theo thời gian chờ như thường
            WAKE_LOCK_TAG,
        ).acquire(holdMillis)
    }

    companion object {
        private const val WAKE_LOCK_TAG = "customaod:lockscreen"
    }
}
