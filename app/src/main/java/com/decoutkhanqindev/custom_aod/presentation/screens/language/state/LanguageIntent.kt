package com.decoutkhanqindev.custom_aod.presentation.screens.language.state

import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue

sealed interface LanguageIntent {
    data class SelectLanguage(val language: LanguageValue) : LanguageIntent
    data object Done : LanguageIntent
    data object Back : LanguageIntent
}
