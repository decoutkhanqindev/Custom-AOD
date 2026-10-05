package com.decoutkhanqindev.custom_aod.data.device.notification

import android.graphics.Bitmap
import androidx.annotation.ColorInt

data class ActiveNotification(
    val key: String,
    val packageName: String,
    val icon: Bitmap,
    @param:ColorInt val color: Int,
    val postTime: Long,
    // Chỉ phần màn hình khoá của hệ thống cho hiện (bản công khai nếu thông báo bị che); null = bị che hẳn.
    val content: NotificationContent?,
)
