package com.decoutkhanqindev.custom_aod.presentation.model.aod.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class ScheduleTimeValue(@param:StringRes val labelRes: Int) {
    START(labelRes = R.string.opt_schedule_start),
    END(labelRes = R.string.opt_schedule_end),
}
