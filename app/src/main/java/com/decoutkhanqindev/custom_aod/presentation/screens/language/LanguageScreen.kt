package com.decoutkhanqindev.custom_aod.presentation.screens.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.MainDestination
import com.decoutkhanqindev.custom_aod.presentation.navigation.PermissionDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.language.state.LanguageEffect
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LanguageScreen(
    backStack: NavBackStack<NavKey>,
    isFirstOpen: Boolean,
) {
    val viewModel: LanguageViewModel = koinViewModel { parametersOf(isFirstOpen) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LanguageEffect.NavigateToPermission -> backStack.navigateTo(PermissionDestination)
                is LanguageEffect.NavigateToMain -> backStack.navigateTo(MainDestination, preserveState = false)
                is LanguageEffect.NavigateBack -> backStack.removeLastOrNull()
            }
        }
    }

    LanguageContent(state = state, onIntent = viewModel::onIntent)
}
