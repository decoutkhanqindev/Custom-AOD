package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

sealed interface AodIntent {
    data object DoubleTap : AodIntent
    data object MediaPlayPause : AodIntent
    data object MediaSkipPrevious : AodIntent
    data object MediaSkipNext : AodIntent
}
