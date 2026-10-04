package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AodScreen(
    isPreview: Boolean,
    onDarkChange: (Boolean) -> Unit,
    onDimChange: (Boolean) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel: AodViewModel = koinViewModel { parametersOf(isPreview) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AodEffect.CloseAod -> onClose()
            }
        }
    }

    LaunchedEffect(state.isDark) {
        onDarkChange(state.isDark)
    }

    LaunchedEffect(state.isDimmed) {
        onDimChange(state.isDimmed)
    }

    BackHandler {
        viewModel.onIntent(AodIntent.PerformGesture(AodGestureValue.BACK))
    }

    AodContent(state = state, onIntent = viewModel::onIntent)
}
