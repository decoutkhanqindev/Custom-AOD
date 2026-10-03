package com.decoutkhanqindev.custom_aod.presentation.aod

import android.os.SystemClock
import com.decoutkhanqindev.custom_aod.data.device.proximity.ProximityManager
import java.lang.ref.WeakReference

// Chỉ AodService và AodActivity dùng, luôn trên main thread, nên field thường là đủ.
class AodSession(
    private val proximityManager: ProximityManager,
) {
    private var current: WeakReference<AodActivity>? = null
    private var isSleepRequested = false

    // Để phân biệt lần bật màn hình của chính app với một lần mở khoá thật.
    var shownAt: Long = 0L
        private set

    val isShowing: Boolean
        get() = current?.get()?.isFinishing == false

    val isCovered: Boolean
        get() = isShowing && proximityManager.isNear.value

    fun attach(activity: AodActivity) {
        current = WeakReference(activity)
        shownAt = SystemClock.elapsedRealtime()
    }

    fun detach(activity: AodActivity) {
        if (current?.get() === activity) current = null
    }

    fun finish() {
        current?.get()?.close()
        current = null
    }

    // Lần tắt màn hình kế tiếp là chủ ý (hết giờ, trong túi, pin yếu): không được mở lại AOD.
    fun requestSleep() {
        isSleepRequested = true
    }

    fun clearSleepRequest() {
        isSleepRequested = false
    }

    fun consumeSleepRequest(): Boolean = isSleepRequested.also { isSleepRequested = false }
}
