package com.decoutkhanqindev.custom_aod.utils

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.packageUri(): Uri = "package:$packageName".toUri()

// Cần Context của Activity (không thêm FLAG_ACTIVITY_NEW_TASK); ROM không có trang đó thì mở Thông tin ứng dụng thay vì crash.
fun Context.openSettingsPage(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: Exception) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri()))
    }
}

fun Context.openOverlaySettings() {
    openSettingsPage(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri()))
}

fun Context.openNotificationSettings() {
    openSettingsPage(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
    )
}

// Android 11+ có trang bật riêng cho từng listener; ROM không có trang đó thì mở danh sách "Truy cập thông báo".
fun Context.openNotificationListenerSettings(listener: ComponentName) {
    try {
        startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, listener.flattenToString()),
        )
    } catch (e: ActivityNotFoundException) {
        openSettingsPage(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

// Trang "Quyền khác" của Xiaomi (MIUI / HyperOS); máy hãng khác không có trang này nên rơi về Thông tin ứng dụng.
fun Context.openMiuiPermissionEditor() {
    openSettingsPage(
        Intent(MIUI_PERMISSION_EDITOR_ACTION)
            .setClassName(MIUI_SECURITY_CENTER_PACKAGE, MIUI_PERMISSION_EDITOR_ACTIVITY)
            .putExtra(MIUI_EXTRA_PACKAGE_NAME, packageName),
    )
}

// Dưới Android 13 chưa có cờ export; nơi gọi chỉ đăng ký broadcast hệ thống được bảo vệ, app khác không gửi được.
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerSystemReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }

private const val MIUI_PERMISSION_EDITOR_ACTION = "miui.intent.action.APP_PERM_EDITOR"
private const val MIUI_SECURITY_CENTER_PACKAGE = "com.miui.securitycenter"
private const val MIUI_PERMISSION_EDITOR_ACTIVITY = "com.miui.permcenter.permissions.PermissionsEditorActivity"
private const val MIUI_EXTRA_PACKAGE_NAME = "extra_pkgname"
