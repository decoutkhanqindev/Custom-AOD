package com.decoutkhanqindev.custom_aod.data.device.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.recoverCatching
import com.decoutkhanqindev.custom_aod.utils.registerSystemReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class BatteryStateManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val batteryChangedFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

    // Receiver chỉ đăng ký khi có người collect (màn AOD): BATTERY_CHANGED gửi rất thường xuyên, service không nên nhận suốt.
    private val batteryChanged: SharedFlow<Intent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent)
            }
        }
        // BATTERY_CHANGED là sticky: đăng ký xong trả luôn giá trị hiện tại.
        app.registerSystemReceiver(receiver, batteryChangedFilter)?.let { trySend(it) }
        awaitClose { app.unregisterReceiver(receiver) }
    }
        .recoverCatching { throwable ->
            Timber.tag(tag).e("batteryChanged have error: ${throwable.stackTraceToString()}")
        }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            replay = 1,
        )

    val levelPercent: StateFlow<Int?> = batteryChanged
        .mapNotNull { it.levelPercent() }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            initialValue = null,
        )

    val isCharging: StateFlow<Boolean?> = batteryChanged
        .mapNotNull { intent -> intent.isCharging().takeIf { intent.levelPercent() != null } }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            initialValue = null,
        )

    fun readLevelPercent(): Int? = readBatteryChanged()?.levelPercent()

    fun readIsCharging(): Boolean? = readBatteryChanged()?.isCharging()

    private fun readBatteryChanged(): Intent? = app.registerSystemReceiver(null, batteryChangedFilter)

    private fun Intent.levelPercent(): Int? {
        val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_SCALE)
        return if (level < 0 || scale <= 0) null else level * 100 / scale
    }

    private fun Intent.isCharging(): Boolean {
        val status = getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    companion object {
        private const val DEFAULT_SCALE = 100
    }
}
