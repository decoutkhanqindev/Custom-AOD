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
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.presentation.aod.AodNotificationListener
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.isGranted
import com.decoutkhanqindev.custom_aod.presentation.navigation.MainDestination
import com.decoutkhanqindev.custom_aod.presentation.navigation.PermissionDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import com.decoutkhanqindev.custom_aod.utils.navigateBack
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import com.decoutkhanqindev.custom_aod.utils.showToast
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber

@Composable
fun CustomizeScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val viewModel: CustomizeViewModel = koinViewModel()
    val permissionManager: PermissionManager = koinInject()
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
        viewModel.effect.filterIsInstance<CustomizeEffect.Decor.OpenBackgroundPicker>()
            .collectCatching(
                block = {
                    backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                catch = { e -> Timber.tag("CustomizeScreen").e(e.stackTraceToString()) },
            )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<CustomizeEffect.Permission>().collectCatching(
            block = { effect ->
                when (effect) {
                    is CustomizeEffect.Permission.RequestCalendarPermission ->
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)

                    is CustomizeEffect.Permission.RequestLocationPermission ->
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)

                    is CustomizeEffect.Permission.OpenNotificationAccessSettings ->
                        AodNotificationListener.openAccessSettings(context) {
                            permissionManager.isGranted(PermissionValue.NOTIFICATION_ACCESS)
                        }
                }
            },
            catch = { e -> Timber.tag("CustomizeScreen").e(e.stackTraceToString()) },
        )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<CustomizeEffect.Navigation>().collectCatching(
            block = { effect ->
                when (effect) {
                    is CustomizeEffect.Navigation.NavigateToPermission -> backStack.add(
                        PermissionDestination
                    )

                    is CustomizeEffect.Navigation.NavigateToMain ->
                        backStack.navigateTo(MainDestination, preserveState = false)

                    is CustomizeEffect.Navigation.NavigateBack -> backStack.navigateBack()
                }
            },
            catch = { e -> Timber.tag("CustomizeScreen").e(e.stackTraceToString()) },
        )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<CustomizeEffect.ShowMessage>().collectCatching(
            block = { effect ->
                context.showToast(resources.getString(effect.messageRes))
            },
            catch = { e -> Timber.tag("CustomizeScreen").e(e.stackTraceToString()) },
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(CustomizeIntent.Permission.RefreshPermissions)
        onPauseOrDispose { }
    }

    BackHandler(enabled = state.isApplying || state.guideStep != null || state.selectedTab != null) {
        when {
            state.isApplying -> Unit
            state.guideStep != null -> viewModel.onIntent(CustomizeIntent.Guide.DismissGuide)
            else -> viewModel.onIntent(CustomizeIntent.Panel.ClosePanel)
        }
    }

    CustomizeContent(state = state, onIntent = viewModel::onIntent)
}
