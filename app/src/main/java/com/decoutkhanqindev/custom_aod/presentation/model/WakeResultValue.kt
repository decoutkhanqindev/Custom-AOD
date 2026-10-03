package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class WakeResultValue(
    val code: Int,
    @param:StringRes val messageRes: Int,
) {
    UNKNOWN(code = 0, messageRes = R.string.wake_unknown),
    OK(code = 1, messageRes = R.string.wake_ok),
    FAILED(code = 2, messageRes = R.string.wake_failed);

    companion object {
        fun fromCode(code: Int?): WakeResultValue = entries.find { it.code == code } ?: UNKNOWN
    }
}
