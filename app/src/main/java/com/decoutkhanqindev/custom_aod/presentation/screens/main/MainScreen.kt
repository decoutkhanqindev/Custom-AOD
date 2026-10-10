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
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.presentation.aod.AodActivity
import com.decoutkhanqindev.custom_aod.presentation.aod.AodNotificationListener
import com.decoutkhanqindev.custom_aod.presentation.aod.AodService
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.areMiuiPermissionsGranted
import com.decoutkhanqindev.custom_aod.presentation.model.permission.isGranted
import com.decoutkhanqindev.custom_aod.presentation.navigation.LanguageDestination
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import com.decoutkhanqindev.custom_aod.utils.openAppSettings
import com.decoutkhanqindev.custom_aod.utils.openMiuiPermissionSettings
import com.decoutkhanqindev.custom_aod.utils.openNotificationSettings
import com.decoutkhanqindev.custom_aod.utils.openOverlaySettings
import com.decoutkhanqindev.custom_aod.utils.showToast
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber

@Composable
fun MainScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val viewModel: MainViewModel = koinViewModel()
    val permissionManager: PermissionManager = koinInject()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.Permission.NotificationPermissionResult(isGranted))
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.Permission.CalendarPermissionResult(isGranted))
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.Permission.LocationPermissionResult(isGranted))
    }
    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        viewModel.onIntent(MainIntent.Appearance.BackgroundPickerResult(uri?.toString()))
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<MainEffect.Service>().collectCatching(
            block = { effect ->
                when (effect) {
                    is MainEffect.Service.StartAodService -> AodService.start(context)
                    is MainEffect.Service.StopAodService -> AodService.stop(context)
                }
            },
            catch = { e -> Timber.tag("MainScreen").e(e.stackTraceToString()) },
        )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<MainEffect.Appearance.OpenBackgroundPicker>()
            .collectCatching(
                block = {
                    backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                catch = { e -> Timber.tag("MainScreen").e(e.stackTraceToString()) },
            )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<MainEffect.Permission>().collectCatching(
            block = { effect ->
                when (effect) {
                    is MainEffect.Permission.OpenOverlaySettings ->
                        context.openOverlaySettings { permissionManager.isGranted(PermissionValue.OVERLAY) }

                    is MainEffect.Permission.OpenMiuiPermissionSettings ->
                        context.openMiuiPermissionSettings { permissionManager.areMiuiPermissionsGranted() }

                    is MainEffect.Permission.OpenNotificationSettings ->
                        context.openNotificationSettings {
                            permissionManager.isGranted(
                                PermissionValue.NOTIFICATIONS
                            )
                        }

                    is MainEffect.Permission.OpenNotificationAccessSettings ->
                        AodNotificationListener.openAccessSettings(context) {
                            permissionManager.isGranted(PermissionValue.NOTIFICATION_ACCESS)
                        }

                    is MainEffect.Permission.OpenAppSettings ->
                        context.openAppSettings { permissionManager.isGranted(effect.permission) }

                    is MainEffect.Permission.RequestNotificationPermission ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }

                    is MainEffect.Permission.RequestCalendarPermission ->
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)

                    is MainEffect.Permission.RequestLocationPermission ->
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            },
            catch = { e -> Timber.tag("MainScreen").e(e.stackTraceToString()) },
        )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<MainEffect.Navigation>().collectCatching(
            block = { effect ->
                when (effect) {
                    is MainEffect.Navigation.NavigateToLanguage -> backStack.add(
                        LanguageDestination(
                            isFirstOpen = false
                        )
                    )

                    is MainEffect.Navigation.OpenPreview -> AodActivity.preview(context)
                }
            },
            catch = { e -> Timber.tag("MainScreen").e(e.stackTraceToString()) },
        )
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.filterIsInstance<MainEffect.ShowMessage>().collectCatching(
            block = { effect ->
                context.showToast(resources.getString(effect.messageRes))
            },
            catch = { e -> Timber.tag("MainScreen").e(e.stackTraceToString()) },
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(MainIntent.Permission.RefreshPermissions)
        onPauseOrDispose { }
    }

    MainContent(state = state, onIntent = viewModel::onIntent)
}
