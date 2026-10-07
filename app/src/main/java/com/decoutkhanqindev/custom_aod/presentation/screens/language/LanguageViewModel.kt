package com.decoutkhanqindev.custom_aod.presentation.screens.language

import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.LanguageValue
import com.decoutkhanqindev.custom_aod.presentation.model.hasRequiredPermissions
import com.decoutkhanqindev.custom_aod.presentation.model.requiredAodPermissions
import com.decoutkhanqindev.custom_aod.presentation.model.toUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageState
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class LanguageViewModel(
    private val isFirstOpen: Boolean,
    private val dataStoreManager: DataStoreManager,
    private val languageManager: LanguageManager,
    private val permissionManager: PermissionManager,
) : BaseViewModel<LanguageState, LanguageIntent, LanguageEffect>(
    initialState = LanguageState(isFirstOpen = isFirstOpen),
), Tag {

    init {
        loadLanguages()
        observeAppliedLanguage()
    }

    override fun onIntent(intent: LanguageIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            is LanguageIntent.SelectLanguage -> updateState { copy(selectedLanguage = intent.language) }
            is LanguageIntent.ConfirmLanguage -> confirmLanguage()
            is LanguageIntent.NavigateBack -> viewModelScope.launch { sendEffect(LanguageEffect.NavigateBack) }
        }
    }

    // Ngôn ngữ của máy lên đầu rồi tới English; lần đầu mở app thì chọn sẵn ngôn ngữ của máy (chưa có bản dịch thì English) để chỉ cần bấm Xong.
    private fun loadLanguages() {
        val deviceLanguage = LanguageValue.fromCode(languageManager.deviceLanguageCode())
            .takeIf { language -> language in LanguageValue.TRANSLATED }
        val languages = LanguageValue.TRANSLATED
            .sortedBy { language ->
                when (language) {
                    deviceLanguage -> 0
                    LanguageValue.DEFAULT -> 1
                    else -> 2
                }
            }
            .map { language -> language.toUiModel(languageManager) }
            .toImmutableList()
        updateState {
            copy(
                languages = languages,
                selectedLanguage = if (isFirstOpen) deviceLanguage ?: LanguageValue.DEFAULT else selectedLanguage,
            )
        }
    }

    private fun observeAppliedLanguage() {
        viewModelScope.launch {
            dataStoreManager.selectedLangCode.filterNotNull().collectCatching(
                action = { code ->
                    val appliedLanguage = LanguageValue.fromCode(code)
                    updateState {
                        copy(appliedLanguage = appliedLanguage, selectedLanguage = selectedLanguage ?: appliedLanguage)
                    }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Lần đầu mở app: chọn xong ngôn ngữ là xong onboarding (đi tiếp sang màn quyền cũng tính là xong), nên thoát giữa chừng
    // thì lần sau vào thẳng màn chính, thiếu quyền thì màn chính nhắc bằng bottom sheet. Máy đã đủ quyền thì bỏ qua màn quyền.
    private fun confirmLanguage() {
        val language = state.value.selectedLanguage ?: return
        dataStoreManager.saveSelectedLangCode(language.code)
        if (isFirstOpen) dataStoreManager.saveIsFirstOpen(false)
        val effect = when {
            !isFirstOpen -> LanguageEffect.NavigateBack
            permissionManager.requiredAodPermissions().hasRequiredPermissions -> LanguageEffect.NavigateToMain
            else -> LanguageEffect.NavigateToPermission
        }
        viewModelScope.launch { sendEffect(effect) }
    }
}
