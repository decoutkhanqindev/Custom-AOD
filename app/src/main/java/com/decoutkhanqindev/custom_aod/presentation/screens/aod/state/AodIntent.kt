package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

sealed interface AodIntent {
    data object DoubleTap : AodIntent
}
