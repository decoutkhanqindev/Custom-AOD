package com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import com.decoutkhanqindev.custom_aod.R

// Ảnh nền AMOLED có sẵn: vector nền đen vẽ thẳng lên AOD (không lưu file) nên nét ở mọi màn hình và không vỡ gradient như JPEG.
// alignment = phần giữ lại khi cắt ảnh dọc cho bố cục ngang (đồng hồ đêm): ảnh có chi tiết ở trên hay dưới thì giữ phần đó.
@Immutable
enum class WallpaperValue(
    val code: Int,
    @param:DrawableRes val drawableRes: Int,
    @param:StringRes val labelRes: Int,
    val alignment: Alignment,
) {
    STARS(
        code = 1,
        drawableRes = R.drawable.img_wallpaper_stars,
        labelRes = R.string.wallpaper_stars,
        alignment = Alignment.Center,
    ),
    AURORA(
        code = 2,
        drawableRes = R.drawable.img_wallpaper_aurora,
        labelRes = R.string.wallpaper_aurora,
        alignment = Alignment.TopCenter,
    ),
    ECLIPSE(
        code = 3,
        drawableRes = R.drawable.img_wallpaper_eclipse,
        labelRes = R.string.wallpaper_eclipse,
        alignment = Alignment.TopCenter,
    ),
    WAVES(
        code = 4,
        drawableRes = R.drawable.img_wallpaper_waves,
        labelRes = R.string.wallpaper_waves,
        alignment = Alignment.BottomCenter,
    ),
    NEBULA(
        code = 5,
        drawableRes = R.drawable.img_wallpaper_nebula,
        labelRes = R.string.wallpaper_nebula,
        alignment = Alignment.Center,
    ),
    HORIZON(
        code = 6,
        drawableRes = R.drawable.img_wallpaper_horizon,
        labelRes = R.string.wallpaper_horizon,
        alignment = Alignment.BottomCenter,
    );

    companion object {
        // Không dùng ảnh có sẵn: không có ảnh nền, hoặc đang dùng ảnh từ máy.
        const val NONE_CODE = 0

        fun fromCode(code: Int?): WallpaperValue? = entries.find { it.code == code }
    }
}
