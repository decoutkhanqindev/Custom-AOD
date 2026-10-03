package com.decoutkhanqindev.custom_aod.data.device.proximity

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.recoverCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class ProximityManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Hết người collect thì huỷ đăng ký cảm biến ngay và trả về false, để lần AOD sau không đọc giá trị cũ.
    val isNear: StateFlow<Boolean> = callbackFlow {
        val sensorManager = app.getSystemService(SensorManager::class.java)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Đa số cảm biến tiệm cận chỉ có 2 mức: 0 là gần, maximumRange là xa.
                trySend(event.values[0] < minOf(event.sensor.maximumRange, NEAR_CM))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.let { sensor ->
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }
        .distinctUntilChanged()
        .recoverCatching { throwable ->
            Timber.tag(tag).e("isNear have error: ${throwable.stackTraceToString()}")
            emit(false)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            initialValue = false,
        )

    companion object {
        private const val NEAR_CM = 5f
    }
}
