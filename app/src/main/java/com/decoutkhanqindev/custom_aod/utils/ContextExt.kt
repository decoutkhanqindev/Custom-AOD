package com.decoutkhanqindev.custom_aod.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// Dưới Android 13 chưa có cờ export; nơi gọi chỉ đăng ký broadcast hệ thống được bảo vệ, app khác không gửi được.
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerSystemReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
