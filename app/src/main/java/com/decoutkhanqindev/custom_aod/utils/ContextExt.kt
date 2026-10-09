package com.decoutkhanqindev.custom_aod.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.decoutkhanqindev.custom_aod.presentation.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.packageUri(): Uri = "package:$packageName".toUri()

// Cần Context của Activity (không thêm FLAG_ACTIVITY_NEW_TASK); ROM không có trang đó thì mở Thông tin ứng dụng thay vì crash.
fun Context.openSettingsPage(vararg intents: Intent): Boolean {
    for (intent in intents) {
        try {
            startActivity(intent)
            return true // Thành công mở được một trang
        } catch (e: Exception) {
            continue // Lỗi trang này thì thử Intent tiếp theo
        }
    }

    // Nếu tất cả Intent truyền vào đều crash/không có trên ROM
    return try {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri()))
        false
    } catch (e: Exception) {
        false
    }
}

fun Context.openOverlaySettings() {
    openSettingsPage(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri()))
}

// Quyền runtime bị từ chối hai lần thì hệ thống không hiện hộp thoại nữa: chỉ cấp được ở trang Thông tin ứng dụng.
fun Context.openAppSettings() {
    openSettingsPage(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri()))
}

fun Context.openNotificationSettings() {
    openSettingsPage(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
            Settings.EXTRA_APP_PACKAGE,
            packageName
        ),
    )
}

// Android 11+ có trang bật riêng cho từng listener; ROM không có trang đó thì mở danh sách "Truy cập thông báo".
fun Context.openNotificationListenerSettings(listener: ComponentName) {
    try {
        startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    listener.flattenToString()
                ),
        )
    } catch (e: ActivityNotFoundException) {
        openSettingsPage(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

// Trang "Quyền khác" của Xiaomi (MIUI / HyperOS); máy hãng khác không có trang này nên rơi về Thông tin ứng dụng.
fun Context.openMiuiPermissionSettings() {
    openSettingsPage(
        Intent(MIUI_PERMISSION_EDITOR_ACTION)
            .setClassName(MIUI_SECURITY_CENTER_PACKAGE, MIUI_PERMISSION_EDITOR_ACTIVITY)
            .putExtra(MIUI_EXTRA_PACKAGE_NAME, packageName),
    )
}

fun Context.openWifiSettings(isConnected: () -> Boolean) {
    val opened = openSettingsPage(
        Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY),
        Intent(Settings.ACTION_WIFI_SETTINGS),
        Intent(Settings.ACTION_WIRELESS_SETTINGS),
        Intent(Settings.ACTION_SETTINGS)
    )

    // Nếu mở thành công bất kỳ trang cài đặt nào, bắt đầu vòng lặp check để return app
    if (opened) returnAppWhen { isConnected() }
}

// Dưới Android 13 chưa có cờ export; nơi gọi chỉ đăng ký broadcast hệ thống được bảo vệ, app khác không gửi được.
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerSystemReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }

fun Context.returnAppWhen(
    timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    condition: () -> Boolean,
) {
    val appContext = this.applicationContext
    val scope = (this.findActivity() as? LifecycleOwner)?.lifecycleScope
        ?: ProcessLifecycleOwner.get().lifecycleScope
    scope.launch {
        try {
            val canReturnApp = withTimeoutOrNull(timeoutMs) {
                delay(FIRST_POLL_DELAY_MS)
                while (!condition()) delay(POLL_INTERVAL_MS)
            } != null
            if (canReturnApp) {
                appContext.startActivity(
                    Intent(appContext, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "checkSettingOn failed")
        }
    }
}

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val TAG = "SettingsReturn"
private const val POLL_INTERVAL_MS = 200L
private const val FIRST_POLL_DELAY_MS = 1_000L
private const val DEFAULT_TIMEOUT_MS = 60_000L

private const val MIUI_PERMISSION_EDITOR_ACTION = "miui.intent.action.APP_PERM_EDITOR"
private const val MIUI_SECURITY_CENTER_PACKAGE = "com.miui.securitycenter"
private const val MIUI_PERMISSION_EDITOR_ACTIVITY =
    "com.miui.permcenter.permissions.PermissionsEditorActivity"
private const val MIUI_EXTRA_PACKAGE_NAME = "extra_pkgname"
