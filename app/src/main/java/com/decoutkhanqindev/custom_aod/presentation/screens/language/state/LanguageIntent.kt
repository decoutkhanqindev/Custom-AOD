package com.decoutkhanqindev.custom_aod.presentation.screens.language.state

import com.decoutkhanqindev.custom_aod.presentation.model.language.LanguageValue

sealed interface LanguageIntent {
    data class SelectLanguage(val language: LanguageValue) : LanguageIntent
    data object ConfirmLanguage : LanguageIntent
    data object NavigateBack : LanguageIntent
}
