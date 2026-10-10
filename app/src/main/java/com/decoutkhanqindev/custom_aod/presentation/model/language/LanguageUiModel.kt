package com.decoutkhanqindev.custom_aod.presentation.model.language

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager

@Immutable
data class LanguageUiModel(
    val language: LanguageValue,
    val displayName: String,
) {
    val label: String get() = "${language.flag}  $displayName"
}

// Tên ngôn ngữ viết bằng chính ngôn ngữ đó, để ai cũng tìm ra ngôn ngữ của mình dù app đang hiện ngôn ngữ nào.
fun LanguageValue.toUiModel(languageManager: LanguageManager): LanguageUiModel =
    LanguageUiModel(
        language = this,
        displayName = languageManager.displayNameOf(code = code, displayIn = code)
    )
