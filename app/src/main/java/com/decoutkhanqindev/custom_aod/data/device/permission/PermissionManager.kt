package com.decoutkhanqindev.custom_aod.data.device.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.Application
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

class PermissionManager(
    private val app: Application,
) {
    val isXiaomi: Boolean
        get() = Build.MANUFACTURER.equals(XIAOMI_MANUFACTURER, ignoreCase = true)

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(app)

    fun areNotificationsEnabled(): Boolean =
        app.getSystemService(NotificationManager::class.java).areNotificationsEnabled()

    fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    fun hasCalendarPermission(): Boolean =
        app.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    fun hasCoarseLocationPermission(): Boolean =
        app.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    // "Truy cập thông báo" là quyền đặc biệt user bật trong Cài đặt, không có checkSelfPermission.
    fun isNotificationListenerEnabled(): Boolean =
        app.packageName in NotificationManagerCompat.getEnabledListenerPackages(app)

    fun isMiuiShowWhenLockedAllowed(): Boolean? = isMiuiOpAllowed(OP_MIUI_SHOW_WHEN_LOCKED)

    fun isMiuiBackgroundStartAllowed(): Boolean? =
        isMiuiOpAllowed(OP_MIUI_BACKGROUND_START_ACTIVITY)

    // Op của Xiaomi không có API công khai và có thể bị đánh số lại theo bản HyperOS: đọc lỗi thì trả null (không rõ).
    @SuppressLint("DiscouragedPrivateApi")
    private fun isMiuiOpAllowed(op: Int): Boolean? = try {
        val appOps = app.getSystemService(AppOpsManager::class.java)
        val checkOp = AppOpsManager::class.java.getMethod(
            "checkOpNoThrow",
            Int::class.java,
            Int::class.java,
            String::class.java,
        )
        checkOp.invoke(appOps, op, Process.myUid(), app.packageName) == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        null
    }

    companion object {
        private const val XIAOMI_MANUFACTURER = "Xiaomi"
        private const val OP_MIUI_SHOW_WHEN_LOCKED = 10020
        private const val OP_MIUI_BACKGROUND_START_ACTIVITY = 10021
    }
}
