package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import android.Manifest
import androidx.activity.compose.BackHandler
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
import com.decoutkhanqindev.custom_aod.presentation.aod.AodNotificationListener
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.navigation.MainDestination
import com.decoutkhanqindev.custom_aod.presentation.navigation.PermissionDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.utils.navigateBack
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import com.decoutkhanqindev.custom_aod.utils.showToast
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomizeScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val viewModel: CustomizeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        viewModel.onIntent(CustomizeIntent.Decor.BackgroundPickerResult(uri?.toString()))
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(CustomizeIntent.Permission.CalendarPermissionResult(isGranted))
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(CustomizeIntent.Permission.LocationPermissionResult(isGranted))
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomizeEffect.Decor.OpenBackgroundPicker ->
                    backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                is CustomizeEffect.Permission.RequestCalendarPermission ->
                    calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)

                is CustomizeEffect.Permission.RequestLocationPermission ->
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)

                is CustomizeEffect.Permission.OpenNotificationAccessSettings ->
                    AodNotificationListener.openAccessSettings(context)

                is CustomizeEffect.ShowMessage -> context.showToast(resources.getString(effect.messageRes))
                is CustomizeEffect.Navigation.NavigateToPermission -> backStack.add(PermissionDestination)
                is CustomizeEffect.Navigation.NavigateToMain ->
                    backStack.navigateTo(MainDestination, preserveState = false)

                is CustomizeEffect.Navigation.NavigateBack -> backStack.navigateBack()
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(CustomizeIntent.Permission.RefreshPermissions)
        onPauseOrDispose { }
    }

    BackHandler(enabled = state.guideStep != null || state.selectedTab != null) {
        viewModel.onIntent(
            if (state.guideStep != null) CustomizeIntent.Guide.DismissGuide else CustomizeIntent.Panel.ClosePanel,
        )
    }

    CustomizeContent(state = state, onIntent = viewModel::onIntent)
}
