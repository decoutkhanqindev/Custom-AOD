package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class ClockFaceValue(
    val code: Int,
    @param:StringRes val labelRes: Int,
) {
    DIGITAL(code = 0, labelRes = R.string.clock_face_digital),
    STACKED(code = 1, labelRes = R.string.clock_face_stacked),
    ANALOG(code = 2, labelRes = R.string.clock_face_analog),
    ANALOG_MINIMAL(code = 3, labelRes = R.string.clock_face_analog_minimal);

    companion object {
        fun fromCode(code: Int?): ClockFaceValue = entries.find { it.code == code } ?: DIGITAL
    }
}
