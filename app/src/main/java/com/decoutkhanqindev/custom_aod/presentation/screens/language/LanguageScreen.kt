package com.decoutkhanqindev.custom_aod.presentation.screens.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.decoutkhanqindev.custom_aod.presentation.components.LocalTag
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.CustomizeDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageEffect
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import com.decoutkhanqindev.custom_aod.utils.navigateBack
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

@Composable
fun LanguageScreen(
    backStack: NavBackStack<NavKey>,
    isFirstOpen: Boolean,
) {
    val tag = LocalTag.current
    val viewModel: LanguageViewModel = koinViewModel { parametersOf(isFirstOpen) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedWithLifecycleEffect {
        viewModel.effect.collectCatching(
            block = { effect ->
                when (effect) {
                    is LanguageEffect.NavigateToCustomize -> backStack.navigateTo(
                        CustomizeDestination
                    )

                    is LanguageEffect.NavigateBack -> backStack.navigateBack()
                }
            },
            catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
        )
    }

    LanguageContent(state = state, onIntent = viewModel::onIntent)
}
