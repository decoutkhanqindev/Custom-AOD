package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R

// UI đọc qua MaterialTheme.typography.
internal object AodsTypography {
    // Inter bản variable (Google Fonts, OFL): một file cho mọi độ đậm, mỗi Font chọn độ đậm bằng trục wght.
    private val FontFamilyInter = FontFamily(
        interFont(FontWeight.Normal),
        interFont(FontWeight.Medium),
        interFont(FontWeight.SemiBold),
        interFont(FontWeight.Bold),
    )

    // Type scale mặc định của Material 3 (kèm lineHeightStyle, platformStyle của nó), chỉ đổi font sang Inter; bodyLarge là bản riêng của app.
    private val MaterialTypeScale = Typography()

    val Material = Typography(
        displayLarge = MaterialTypeScale.displayLarge.copy(fontFamily = FontFamilyInter),
        displayMedium = MaterialTypeScale.displayMedium.copy(fontFamily = FontFamilyInter),
        displaySmall = MaterialTypeScale.displaySmall.copy(fontFamily = FontFamilyInter),
        headlineLarge = MaterialTypeScale.headlineLarge.copy(fontFamily = FontFamilyInter),
        headlineMedium = MaterialTypeScale.headlineMedium.copy(fontFamily = FontFamilyInter),
        headlineSmall = MaterialTypeScale.headlineSmall.copy(fontFamily = FontFamilyInter),
        titleLarge = MaterialTypeScale.titleLarge.copy(fontFamily = FontFamilyInter),
        titleMedium = MaterialTypeScale.titleMedium.copy(fontFamily = FontFamilyInter),
        titleSmall = MaterialTypeScale.titleSmall.copy(fontFamily = FontFamilyInter),
        bodyLarge = TextStyle(
            fontFamily = FontFamilyInter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = MaterialTypeScale.bodyMedium.copy(fontFamily = FontFamilyInter),
        bodySmall = MaterialTypeScale.bodySmall.copy(fontFamily = FontFamilyInter),
        labelLarge = MaterialTypeScale.labelLarge.copy(fontFamily = FontFamilyInter),
        labelMedium = MaterialTypeScale.labelMedium.copy(fontFamily = FontFamilyInter),
        labelSmall = MaterialTypeScale.labelSmall.copy(fontFamily = FontFamilyInter),
    )

    private fun interFont(weight: FontWeight): Font = Font(
        resId = R.font.inter_variable,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )
}
