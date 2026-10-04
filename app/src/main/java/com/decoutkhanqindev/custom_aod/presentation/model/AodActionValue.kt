package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class AodActionValue(
    val code: Int,
    @param:StringRes val labelRes: Int,
) {
    NONE(code = 0, labelRes = R.string.aod_action_none),
    CLOSE(code = 1, labelRes = R.string.aod_action_close),
    GO_DARK(code = 2, labelRes = R.string.aod_action_go_dark),
    FLASHLIGHT(code = 3, labelRes = R.string.aod_action_flashlight),
    PLAY_PAUSE(code = 4, labelRes = R.string.aod_action_play_pause),
    PREVIOUS_TRACK(code = 5, labelRes = R.string.aod_action_previous_track),
    NEXT_TRACK(code = 6, labelRes = R.string.aod_action_next_track);

    companion object {
        fun fromCode(code: Int?): AodActionValue = entries.find { it.code == code } ?: NONE
    }
}
