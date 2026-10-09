package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

// Các cụm tab ở màn thử tùy chỉnh, đúng thứ tự trên thanh tab; hướng dẫn đi qua từng cụm.
@Immutable
enum class CustomizeClusterValue(@param:StringRes val labelRes: Int) {
    APPEARANCE(labelRes = R.string.customize_cluster_appearance),
    DECOR(labelRes = R.string.customize_cluster_decor),
    INFO(labelRes = R.string.customize_cluster_info),
    EFFECT(labelRes = R.string.customize_cluster_effect),
}
