package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

// cluster = cụm tab được làm sáng; PREVIEW không làm sáng gì, ACTIONS làm sáng thanh công cụ dưới top bar.
@Immutable
enum class CustomizeGuideStepValue(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val cluster: CustomizeClusterValue? = null,
) {
    PREVIEW(R.string.customize_guide_preview_title, R.string.customize_guide_preview_desc),
    APPEARANCE(
        R.string.customize_guide_appearance_title,
        R.string.customize_guide_appearance_desc,
        CustomizeClusterValue.APPEARANCE,
    ),
    DECOR(R.string.customize_guide_decor_title, R.string.customize_guide_decor_desc, CustomizeClusterValue.DECOR),
    INFO(R.string.customize_guide_info_title, R.string.customize_guide_info_desc, CustomizeClusterValue.INFO),
    EFFECT(R.string.customize_guide_effect_title, R.string.customize_guide_effect_desc, CustomizeClusterValue.EFFECT),
    ACTIONS(R.string.customize_guide_actions_title, R.string.customize_guide_actions_desc),
    ;

    val number: Int
        get() = ordinal + 1

    val next: CustomizeGuideStepValue?
        get() = entries.getOrNull(ordinal + 1)
}
