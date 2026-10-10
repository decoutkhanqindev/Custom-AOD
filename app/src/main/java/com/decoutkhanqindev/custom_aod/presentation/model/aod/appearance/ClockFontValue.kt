package com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import com.decoutkhanqindev.custom_aod.R

// Họ font chung của hệ thống: không cần file font hay thư viện, mỗi ROM tự chọn font cụ thể.
@Immutable
enum class ClockFontValue(
    val code: Int,
    @param:StringRes val labelRes: Int,
    val fontFamily: FontFamily,
) {
    DEFAULT(code = 0, labelRes = R.string.clock_font_default, fontFamily = FontFamily.Default),
    SERIF(code = 1, labelRes = R.string.clock_font_serif, fontFamily = FontFamily.Serif),
    MONOSPACE(
        code = 2,
        labelRes = R.string.clock_font_monospace,
        fontFamily = FontFamily.Monospace
    ),
    CURSIVE(code = 3, labelRes = R.string.clock_font_cursive, fontFamily = FontFamily.Cursive);

    companion object {
        fun fromCode(code: Int?): ClockFontValue = entries.find { it.code == code } ?: DEFAULT
    }
}
