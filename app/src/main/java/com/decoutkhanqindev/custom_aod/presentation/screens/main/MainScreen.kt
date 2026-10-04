package com.decoutkhanqindev.custom_aod.presentation.screens.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.decoutkhanqindev.custom_aod.presentation.aod.AodActivity
import com.decoutkhanqindev.custom_aod.presentation.aod.AodService
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.LanguageDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.utils.openMiuiPermissionEditor
import com.decoutkhanqindev.custom_aod.utils.openNotificationSettings
import com.decoutkhanqindev.custom_aod.utils.openOverlaySettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val viewModel: MainViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.NotificationPermissionResult(isGranted))
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.StartAodService -> AodService.start(context)
                is MainEffect.StopAodService -> AodService.stop(context)
                is MainEffect.OpenPreview -> AodActivity.preview(context)
                is MainEffect.OpenOverlaySettings -> context.openOverlaySettings()
                is MainEffect.OpenMiuiPermissionEditor -> context.openMiuiPermissionEditor()
                is MainEffect.OpenNotificationSettings -> context.openNotificationSettings()
                is MainEffect.RequestNotificationPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                is MainEffect.NavigateToLanguage -> backStack.add(LanguageDestination(isFirstOpen = false))
            }
        }
    }

    LifecycleResumeEffect(state.isNotificationPermissionPending) {
        if (!state.isNotificationPermissionPending) return@LifecycleResumeEffect onPauseOrDispose { }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.onIntent(MainIntent.NotificationPermissionRequested)
        onPauseOrDispose { }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(MainIntent.RefreshPermissions)
        onPauseOrDispose { }
    }

    MainContent(state = state, onIntent = viewModel::onIntent)
}
