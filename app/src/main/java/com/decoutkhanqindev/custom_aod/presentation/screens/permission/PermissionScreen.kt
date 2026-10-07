package com.decoutkhanqindev.custom_aod.presentation.screens.permission

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
import com.decoutkhanqindev.custom_aod.presentation.aod.AodService
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.MainDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.state.PermissionIntent
import com.decoutkhanqindev.custom_aod.utils.navigateBack
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import com.decoutkhanqindev.custom_aod.utils.openMiuiPermissionSettings
import com.decoutkhanqindev.custom_aod.utils.openNotificationSettings
import com.decoutkhanqindev.custom_aod.utils.openOverlaySettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PermissionScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val viewModel: PermissionViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(PermissionIntent.NotificationPermissionResult(isGranted))
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PermissionEffect.OpenOverlaySettings -> context.openOverlaySettings()
                is PermissionEffect.OpenMiuiPermissionSettings -> context.openMiuiPermissionSettings()
                is PermissionEffect.OpenNotificationSettings -> context.openNotificationSettings()
                is PermissionEffect.RequestNotificationPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                is PermissionEffect.StartAodService -> AodService.start(context)
                is PermissionEffect.NavigateToMain -> backStack.navigateTo(MainDestination, preserveState = false)
                is PermissionEffect.NavigateBack -> backStack.navigateBack()
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(PermissionIntent.RefreshPermissions)
        onPauseOrDispose { }
    }

    PermissionContent(state = state, onIntent = viewModel::onIntent)
}
