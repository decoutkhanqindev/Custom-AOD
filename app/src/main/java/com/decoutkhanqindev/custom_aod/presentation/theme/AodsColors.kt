package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.ui.graphics.Color

// Bảng màu thô: chỉ AodsTheme.kt (dựng color scheme) và bảng màu đồng hồ cho user chọn (ClockColorValue) đọc thẳng; UI đọc qua MaterialTheme.colorScheme.
internal object AodsColors {
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val Mint = Color(0xFF7FD1AE)
    val Red = Color(0xFFE5484D)
    val Blue = Color(0xFF8AB4F8)
    val Purple = Color(0xFFC3A6FF)
    val Orange = Color(0xFFFFB27A)
    val Pink = Color(0xFFFF9EC4)
    val GreyED = Color(0xFFEDEDED)
    val GreyB4 = Color(0xFFB4B4B4)
    val Grey9A = Color(0xFF9A9A9A)
    val Grey8A = Color(0xFF8A8A8A)
    val Grey6E = Color(0xFF6E6E6E)
    val Grey5A = Color(0xFF5A5A5A)

    // Ba màu trung tính mặc định của Material 3 (bản tối) mà app vẫn dùng, giữ đúng giá trị để giao diện không đổi.
    val Neutral12 = Color(0xFF211F26)
    val NeutralVariant30 = Color(0xFF49454F)
    val NeutralVariant60 = Color(0xFF938F99)

}
