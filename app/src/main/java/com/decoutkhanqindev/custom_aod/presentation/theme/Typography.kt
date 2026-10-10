package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R

// Inter bản variable (Google Fonts, OFL): một file cho mọi độ đậm, mỗi Font chọn độ đậm bằng trục wght.
val FontFamilyInter = FontFamily(
    interFont(FontWeight.Normal),
    interFont(FontWeight.Medium),
    interFont(FontWeight.SemiBold),
    interFont(FontWeight.Bold),
)

// Type scale mặc định của Material 3 (kèm lineHeightStyle, platformStyle của nó), chỉ đổi font sang Inter; BodyLarge là bản riêng của app.
private val MaterialTypeScale = androidx.compose.material3.Typography()

val DisplaySmall = MaterialTypeScale.displaySmall.copy(fontFamily = FontFamilyInter)
val HeadlineLarge = MaterialTypeScale.headlineLarge.copy(fontFamily = FontFamilyInter)
val HeadlineMedium = MaterialTypeScale.headlineMedium.copy(fontFamily = FontFamilyInter)
val HeadlineSmall = MaterialTypeScale.headlineSmall.copy(fontFamily = FontFamilyInter)
val TitleLarge = MaterialTypeScale.titleLarge.copy(fontFamily = FontFamilyInter)
val TitleMedium = MaterialTypeScale.titleMedium.copy(fontFamily = FontFamilyInter)
val BodyLarge = TextStyle(
    fontFamily = FontFamilyInter,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
)
val BodyMedium = MaterialTypeScale.bodyMedium.copy(fontFamily = FontFamilyInter)
val BodySmall = MaterialTypeScale.bodySmall.copy(fontFamily = FontFamilyInter)
val LabelLarge = MaterialTypeScale.labelLarge.copy(fontFamily = FontFamilyInter)
val LabelMedium = MaterialTypeScale.labelMedium.copy(fontFamily = FontFamilyInter)

// Cầu nối cho component Material 3: style app không dùng vẫn theo type scale của Material 3 nhưng đổi sang Inter.
val MaterialTypography = androidx.compose.material3.Typography(
    displayLarge = MaterialTypeScale.displayLarge.copy(fontFamily = FontFamilyInter),
    displayMedium = MaterialTypeScale.displayMedium.copy(fontFamily = FontFamilyInter),
    displaySmall = DisplaySmall,
    headlineLarge = HeadlineLarge,
    headlineMedium = HeadlineMedium,
    headlineSmall = HeadlineSmall,
    titleLarge = TitleLarge,
    titleMedium = TitleMedium,
    titleSmall = MaterialTypeScale.titleSmall.copy(fontFamily = FontFamilyInter),
    bodyLarge = BodyLarge,
    bodyMedium = BodyMedium,
    bodySmall = BodySmall,
    labelLarge = LabelLarge,
    labelMedium = LabelMedium,
    labelSmall = MaterialTypeScale.labelSmall.copy(fontFamily = FontFamilyInter),
)

private fun interFont(weight: FontWeight): Font = Font(
    resId = R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)
