package com.decoutkhanqindev.custom_aod.presentation.screens.language.state

sealed interface LanguageEffect {
    data object NavigateToPermission : LanguageEffect
    data object NavigateToMain : LanguageEffect
    data object NavigateBack : LanguageEffect
}
