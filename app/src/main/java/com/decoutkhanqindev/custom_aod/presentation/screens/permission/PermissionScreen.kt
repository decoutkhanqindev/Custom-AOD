package com.decoutkhanqindev.custom_aod.presentation.screens.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.MainDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionIntent
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import com.decoutkhanqindev.custom_aod.utils.openMiuiPermissionSettings
import com.decoutkhanqindev.custom_aod.utils.openOverlaySettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PermissionScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val viewModel: PermissionViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PermissionEffect.OpenOverlaySettings -> context.openOverlaySettings()
                is PermissionEffect.OpenMiuiPermissionSettings -> context.openMiuiPermissionSettings()
                is PermissionEffect.NavigateToMain -> backStack.navigateTo(MainDestination, preserveState = false)
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(PermissionIntent.RefreshPermissions)
        onPauseOrDispose { }
    }

    PermissionContent(state = state, onIntent = viewModel::onIntent)
}
