package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.presentation.components.aod.AodContent
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

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
        viewModel.effect.filterIsInstance<AodEffect.CloseAod>().collectCatching(
            block = { onClose() },
            catch = { e -> Timber.tag("AodScreen").e(e.stackTraceToString()) },
        )
    }

    SideEffect(state.isDark) {
        onDarkChange(state.isDark)
    }

    SideEffect(state.isDimmed) {
        onDimChange(state.isDimmed)
    }

    BackHandler {
        viewModel.onIntent(AodIntent.PerformGesture(AodGestureValue.BACK))
    }

    AodContent(state = state, onIntent = viewModel::onIntent)
}
