package com.decoutkhanqindev.custom_aod.presentation.theme.tokens

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R

object AodsPrimitiveTypography {
    // Inter bản variable (Google Fonts, OFL): một file cho mọi độ đậm, mỗi Font chọn độ đậm bằng trục wght.
    val FontFamilyInter = FontFamily(
        interFont(FontWeight.Normal),
        interFont(FontWeight.Medium),
        interFont(FontWeight.SemiBold),
        interFont(FontWeight.Bold),
    )

    val FontSm = 12.sp
    val FontSmPlus = 13.sp
    val FontMd = 14.sp
    val FontLg = 16.sp
    val FontClock = 76.sp
    val FontClockStacked = 96.sp

    val LineHeightLg = 24.sp

    val LetterSpacingBody = 0.5.sp

    private fun interFont(weight: FontWeight): Font = Font(
        resId = R.font.inter_variable,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )
}
