package com.decoutkhanqindev.custom_aod.data.device.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.withContextCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.coroutines.resume

class DeviceLocationManager(
    private val app: Application,
) : Tag {
    private val locationManager = app.getSystemService(LocationManager::class.java)

    // Vị trí gần đúng (Wi-Fi, mạng di động) đủ cho thời tiết, nhanh và ít tốn pin hơn GPS; chưa có bản mới thì dùng vị trí đã biết gần nhất. Không có quyền hoặc tắt định vị thì null.
    suspend fun currentCoarseLocation(): Coordinates? {
        if (app.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return withContextCatching(
            context = Dispatchers.IO,
            block = {
                val location =
                    withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { requestCurrentLocation() }
                        ?: lastKnownLocation()
                location?.let { Coordinates(latitude = it.latitude, longitude = it.longitude) }
            },
            catch = { e ->
                Timber.tag(tag).e("Could not read the location: ${e.stackTraceToString()}")
                null
            },
        )
    }

    // Nơi gọi đã kiểm quyền vị trí.
    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): Location? {
        val provider = preferredProvider() ?: return null
        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            locationManager.getCurrentLocation(
                provider,
                cancellationSignal,
                app.mainExecutor
            ) { location ->
                if (continuation.isActive) continuation.resume(location)
            }
        }
    }

    // Nơi gọi đã kiểm quyền vị trí.
    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): Location? = locationManager.getProviders(true)
        .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
        .maxByOrNull { location -> location.time }

    // Fused (có Google Play services) gộp Wi-Fi và mạng di động; không có thì dùng nhà cung cấp mạng. Quyền gần đúng không dùng được GPS.
    private fun preferredProvider(): String? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER

        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    companion object {
        private const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }
}
