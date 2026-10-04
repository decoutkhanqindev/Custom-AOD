package com.decoutkhanqindev.custom_aod.data.device.pickup

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.recoverCatching
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

class PickupGestureManager(
    private val app: Application,
) : Tag {
    private val sensorManager = app.getSystemService(SensorManager::class.java)

    // Cảm biến "nhấc máy" chuẩn của Android (hệ thống dùng cho "Nhấc lên để kiểm tra"): wake-up, one-shot, chạy trên chip cảm biến nên chờ lúc màn hình tắt gần như không tốn pin. Kiểu này bị ẩn trong SDK nên tìm theo tên; máy không có thì không hỗ trợ.
    private val pickupSensor: Sensor? = sensorManager.getSensorList(Sensor.TYPE_ALL).firstOrNull { sensor ->
        sensor.stringType == PICK_UP_GESTURE_TYPE &&
            sensor.isWakeUpSensor &&
            sensor.reportingMode == Sensor.REPORTING_MODE_ONE_SHOT
    }

    val isSupported: Boolean
        get() = pickupSensor != null

    // Phát một lần khi nhấc máy; huỷ collect thì huỷ chờ. Không có cảm biến thì kết thúc ngay, không phát gì.
    val pickups: Flow<Unit> = callbackFlow {
        val sensor = pickupSensor
        val listener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                trySend(Unit)
            }
        }
        if (sensor == null || !sensorManager.requestTriggerSensor(listener, sensor)) {
            close()
            return@callbackFlow
        }
        awaitClose { sensorManager.cancelTriggerSensor(listener, sensor) }
    }
        .recoverCatching { throwable ->
            Timber.tag(tag).e("pickups have error: ${throwable.stackTraceToString()}")
        }

    companion object {
        private const val PICK_UP_GESTURE_TYPE = "android.sensor.pick_up_gesture"
    }
}
