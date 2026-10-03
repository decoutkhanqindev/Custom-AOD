package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodEffect
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AodScreen(
    isPreview: Boolean,
    onDarkChange: (Boolean) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel: AodViewModel = koinViewModel { parametersOf(isPreview) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AodEffect.Close -> onClose()
            }
        }
    }

    LaunchedEffect(state.isDark) {
        onDarkChange(state.isDark)
    }

    AodContent(state = state, onIntent = viewModel::onIntent)
}
