package com.decoutkhanqindev.custom_aod.presentation.screens.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.presentation.aod.AodActivity
import com.decoutkhanqindev.custom_aod.presentation.aod.AodService
import com.decoutkhanqindev.custom_aod.presentation.effects.LaunchedWithLifecycleEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: MainViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.NotificationPermissionResult(isGranted))
    }
    val firstLaunchNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        viewModel.onIntent(MainIntent.FirstLaunchNotificationPermissionResult(isGranted))
    }

    LaunchedWithLifecycleEffect {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MainEffect.StartAodService -> AodService.start(context)
                is MainEffect.StopAodService -> AodService.stop(context)
                is MainEffect.OpenPreview -> AodActivity.preview(context)
                is MainEffect.OpenOverlaySettings -> context.openSettingsPage(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, context.packageUri()),
                )

                is MainEffect.OpenMiuiPermissionEditor -> context.openSettingsPage(
                    Intent(MIUI_PERMISSION_EDITOR_ACTION)
                        .setClassName(MIUI_SECURITY_CENTER_PACKAGE, MIUI_PERMISSION_EDITOR_ACTIVITY)
                        .putExtra(MIUI_EXTRA_PACKAGE_NAME, context.packageName),
                )

                is MainEffect.OpenNotificationSettings -> context.openSettingsPage(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                )

                is MainEffect.RequestNotificationPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
            }
        }
    }

    LaunchedEffect(state.isNotificationPermissionPending) {
        if (!state.isNotificationPermissionPending) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            firstLaunchNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        viewModel.onIntent(MainIntent.NotificationPermissionRequested)
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(MainIntent.RefreshPermissions)
        onPauseOrDispose { }
    }

    MainContent(state = state, onIntent = viewModel::onIntent)
}

private fun Context.packageUri(): Uri = "package:$packageName".toUri()

private fun Context.openSettingsPage(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: Exception) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri()))
    }
}

private const val MIUI_PERMISSION_EDITOR_ACTION = "miui.intent.action.APP_PERM_EDITOR"
private const val MIUI_SECURITY_CENTER_PACKAGE = "com.miui.securitycenter"
private const val MIUI_PERMISSION_EDITOR_ACTIVITY = "com.miui.permcenter.permissions.PermissionsEditorActivity"
private const val MIUI_EXTRA_PACKAGE_NAME = "extra_pkgname"
