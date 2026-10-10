package com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.theme.Blue
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyB4
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyED
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import com.decoutkhanqindev.custom_aod.presentation.theme.Orange
import com.decoutkhanqindev.custom_aod.presentation.theme.Pink
import com.decoutkhanqindev.custom_aod.presentation.theme.Purple
import com.decoutkhanqindev.custom_aod.presentation.theme.Red

// Màu dịu trên nền đen; mặc định xám như trước để ít sáng, ít burn-in.
@Immutable
enum class ClockColorValue(
    val code: Int,
    @param:StringRes val labelRes: Int,
    val color: Color,
) {
    GREY(code = 0, labelRes = R.string.clock_color_grey, color = GreyB4),
    WHITE(code = 1, labelRes = R.string.clock_color_white, color = GreyED),
    MINT(code = 2, labelRes = R.string.clock_color_mint, color = Mint),
    BLUE(code = 3, labelRes = R.string.clock_color_blue, color = Blue),
    PURPLE(code = 4, labelRes = R.string.clock_color_purple, color = Purple),
    ORANGE(code = 5, labelRes = R.string.clock_color_orange, color = Orange),
    PINK(code = 6, labelRes = R.string.clock_color_pink, color = Pink),
    RED(code = 7, labelRes = R.string.clock_color_red, color = Red);

    companion object {
        fun fromCode(code: Int?): ClockColorValue = entries.find { it.code == code } ?: GREY
    }
}
