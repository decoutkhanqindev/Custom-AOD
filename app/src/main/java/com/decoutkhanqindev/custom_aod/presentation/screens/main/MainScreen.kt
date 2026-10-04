package com.decoutkhanqindev.custom_aod.presentation.screens.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.decoutkhanqindev.custom_aod.presentation.aod.AodActivity
import com.decoutkhanqindev.custom_aod.presentation.aod.AodNotificationListener
import com.decoutkhanqindev.custom_aod.presentation.aod.AodService
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.LanguageDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.utils.openMiuiPermissionSettings
import com.decoutkhanqindev.custom_aod.utils.openNotificationSettings
import com.decoutkhanqindev.custom_aod.utils.openOverlaySettings
import com.decoutkhanqindev.custom_aod.utils.showToast
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val viewModel: MainViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.NotificationPermissionResult(isGranted))
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.CalendarPermissionResult(isGranted))
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.LocationPermissionResult(isGranted))
    }
    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        viewModel.onIntent(MainIntent.BackgroundPickerResult(uri?.toString()))
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.StartAodService -> AodService.start(context)
                is MainEffect.StopAodService -> AodService.stop(context)
                is MainEffect.OpenPreview -> AodActivity.preview(context)
                is MainEffect.OpenOverlaySettings -> context.openOverlaySettings()
                is MainEffect.OpenMiuiPermissionSettings -> context.openMiuiPermissionSettings()
                is MainEffect.OpenNotificationSettings -> context.openNotificationSettings()
                is MainEffect.OpenNotificationAccessSettings ->
                    AodNotificationListener.openAccessSettings(context)

                is MainEffect.RequestNotificationPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                is MainEffect.RequestCalendarPermission ->
                    calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)

                is MainEffect.RequestLocationPermission ->
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)

                is MainEffect.NavigateToLanguage -> backStack.add(LanguageDestination(isFirstOpen = false))
                is MainEffect.OpenBackgroundPicker ->
                    backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                is MainEffect.ShowMessage -> context.showToast(resources.getString(effect.messageRes))
            }
        }
    }

    LifecycleResumeEffect(state.isNotificationPermissionPending) {
        if (!state.isNotificationPermissionPending) return@LifecycleResumeEffect onPauseOrDispose { }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.onIntent(MainIntent.NotificationPermissionDialogShown)
        onPauseOrDispose { }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(MainIntent.RefreshPermissions)
        onPauseOrDispose { }
    }

    MainContent(state = state, onIntent = viewModel::onIntent)
}
