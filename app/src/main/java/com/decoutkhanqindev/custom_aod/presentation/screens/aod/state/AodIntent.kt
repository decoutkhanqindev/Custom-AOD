package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodGestureValue

sealed interface AodIntent {
    data class PerformGesture(val gesture: AodGestureValue) : AodIntent
    data object PlayPauseMedia : AodIntent
    data object SkipToPreviousTrack : AodIntent
    data object SkipToNextTrack : AodIntent
}
