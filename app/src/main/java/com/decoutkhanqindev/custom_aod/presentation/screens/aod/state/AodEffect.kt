package com.decoutkhanqindev.custom_aod.presentation.screens.aod.state

sealed interface AodEffect {
    data object CloseAod : AodEffect
}
