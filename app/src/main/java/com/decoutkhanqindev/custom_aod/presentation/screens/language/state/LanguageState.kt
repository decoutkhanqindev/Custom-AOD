package com.decoutkhanqindev.custom_aod.presentation.screens.language.state

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class LanguageState(
    val isFirstOpen: Boolean = false,
    val languages: ImmutableList<LanguageUiModel> = persistentListOf(),
    val appliedLanguage: LanguageValue? = null,
    val selectedLanguage: LanguageValue? = null,
) {
    // Mở từ màn chính mà chọn lại đúng ngôn ngữ đang dùng thì không có gì để lưu; lần đầu mở app thì luôn phải bấm Xong để đi tiếp.
    val isConfirmEnabled: Boolean
        get() = selectedLanguage != null && (isFirstOpen || selectedLanguage != appliedLanguage)
}
