package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle

// App luôn tối: một bộ màu, không theo sáng/tối của hệ thống. UI đọc màu, chữ, bo góc qua MaterialTheme.
private val AodsColorScheme = darkColorScheme(
    primary = AodsColors.Mint,
    onPrimary = AodsColors.Black,
    background = AodsColors.Black,
    onBackground = AodsColors.GreyED,
    surface = AodsColors.Black,
    onSurface = AodsColors.GreyED,
    surfaceVariant = AodsColors.NeutralVariant30,
    onSurfaceVariant = AodsColors.Grey9A,
    surfaceContainer = AodsColors.Neutral12,
    outline = AodsColors.NeutralVariant60,
    outlineVariant = AodsColors.NeutralVariant30,
    error = AodsColors.Red,
    // Trắng trên nền đen: nét vẽ ở bảng vẽ, icon trên lớp scrim, vệt sáng shimmer.
    inverseSurface = AodsColors.White,
    scrim = AodsColors.Black,
)

// Màn AOD: xám thay vì trắng cho ít sáng, ít tốn pin, ít burn-in. onBackground chữ chính, onSurfaceVariant chữ phụ,
// outline chữ gợi ý / nút bị tắt, secondary nút media, primary màu nhấn mặc định.
private val AodsAodColorScheme = darkColorScheme(
    primary = AodsColors.Mint,
    secondary = AodsColors.GreyB4,
    background = AodsColors.Black,
    onBackground = AodsColors.Grey8A,
    onSurfaceVariant = AodsColors.Grey6E,
    outline = AodsColors.Grey5A,
)

@Composable
fun AodsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AodsColorScheme,
        typography = AodsTypography.Material,
        shapes = AodsShapes.Material,
        content = content,
    )
}

// Chữ màn AOD dùng font hệ thống / font đồng hồ user chọn, nên trả LocalTextStyle về mặc định: MaterialTheme đặt bodyLarge
// (lineHeight 24sp) làm đồng hồ 76sp đè lên dòng ngày.
@Composable
fun AodsAodTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AodsAodColorScheme) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle.Default, content = content)
    }
}
