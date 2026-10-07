package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors

// Màu dịu trên nền đen; mặc định xám như trước để ít sáng, ít burn-in.
@Immutable
enum class ClockColorValue(
    val code: Int,
    @param:StringRes val labelRes: Int,
    val color: Color,
) {
    GREY(code = 0, labelRes = R.string.clock_color_grey, color = AodsColors.GreyB4),
    WHITE(code = 1, labelRes = R.string.clock_color_white, color = AodsColors.GreyED),
    MINT(code = 2, labelRes = R.string.clock_color_mint, color = AodsColors.Mint),
    BLUE(code = 3, labelRes = R.string.clock_color_blue, color = AodsColors.Blue),
    PURPLE(code = 4, labelRes = R.string.clock_color_purple, color = AodsColors.Purple),
    ORANGE(code = 5, labelRes = R.string.clock_color_orange, color = AodsColors.Orange),
    PINK(code = 6, labelRes = R.string.clock_color_pink, color = AodsColors.Pink),
    RED(code = 7, labelRes = R.string.clock_color_red, color = AodsColors.Red);

    companion object {
        fun fromCode(code: Int?): ClockColorValue = entries.find { it.code == code } ?: GREY
    }
}
