package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.aod.AodContent
import com.decoutkhanqindev.custom_aod.presentation.components.aod.DrawingPadDialog
import com.decoutkhanqindev.custom_aod.presentation.components.aod.MemoEditorDialog
import com.decoutkhanqindev.custom_aod.presentation.components.onboarding.AppOnboardingTopBar
import com.decoutkhanqindev.custom_aod.presentation.components.permission.AppPermissionSheet
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeGuideStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.model.onboarding.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.components.CustomizeApplyingOverlay
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.components.CustomizeGuideOverlay
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.components.CustomizePanelHost
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.components.CustomizeTabBar
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.components.CustomizeToolbar
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeState
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey9A
import com.decoutkhanqindev.custom_aod.presentation.theme.Theme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CustomizeContent(
    state: CustomizeState,
    onIntent: (CustomizeIntent) -> Unit,
) {
    val preview = rememberCustomizePreview(state)
    val guideTargets = remember { mutableStateMapOf<CustomizeGuideStepValue, Rect>() }
    val onTargetPositioned = remember(guideTargets) {
        { step: CustomizeGuideStepValue, bounds: Rect ->
            if (guideTargets[step] != bounds) guideTargets[step] = bounds
        }
    }
    var lastGuideStep by remember { mutableStateOf(CustomizeGuideStepValue.PREVIEW) }

    SideEffect(state.guideStep) {
        state.guideStep?.let { step -> lastGuideStep = step }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
    ) {
        AodContent(state = preview, onIntent = {})

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppOnboardingTopBar(
                step = OnboardingStepValue.CUSTOMIZE,
                onNavigateBack = { onIntent(CustomizeIntent.NavigateBack) },
                containerColor = Color.Transparent,
                actions = {
                    IconButton(onClick = { onIntent(CustomizeIntent.Guide.ShowGuide) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.customize_show_guide),
                            tint = Grey9A,
                        )
                    }

                    TextButton(
                        onClick = { onIntent(CustomizeIntent.SkipCustomization) },
                        enabled = !state.isApplying,
                    ) {
                        Text(text = stringResource(R.string.customize_skip))
                    }
                },
            )

            CustomizeToolbar(
                history = state.history,
                isApplying = state.isApplying || state.isLoading,
                onIntent = onIntent,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    onTargetPositioned(
                        CustomizeGuideStepValue.ACTIONS,
                        coordinates.boundsInWindow()
                    )
                },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            CustomizePanelHost(
                selectedTab = state.selectedTab,
                draft = state.draft,
                decor = state.decor,
                onIntent = onIntent,
            )

            CustomizeTabBar(
                selectedTab = state.selectedTab,
                guideStep = state.guideStep,
                onIntent = onIntent,
                onClusterPositioned = onTargetPositioned,
            )
        }

        AnimatedVisibility(
            visible = state.guideStep != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CustomizeGuideOverlay(
                step = state.guideStep ?: lastGuideStep,
                targets = guideTargets,
                onIntent = onIntent,
            )
        }

        AnimatedVisibility(
            visible = state.isApplying,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CustomizeApplyingOverlay()
        }
    }

    CustomizePermissionSheet(permission = state.permissionRequest, onIntent = onIntent)

    if (state.history.isResetConfirmVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(CustomizeIntent.History.DismissResetConfirm) },
            confirmButton = {
                TextButton(onClick = { onIntent(CustomizeIntent.History.ResetCustomization) }) {
                    Text(text = stringResource(R.string.customize_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(CustomizeIntent.History.DismissResetConfirm) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.customize_reset_title)) },
            text = { Text(text = stringResource(R.string.customize_reset_desc)) },
        )
    }

    if (state.decor.isDrawingPadVisible) {
        DrawingPadDialog(
            onConfirm = { drawing -> onIntent(CustomizeIntent.Decor.ChangeDrawing(drawing)) },
            onDismiss = { onIntent(CustomizeIntent.Decor.DismissDrawingPad) },
        )
    }

    if (state.decor.isMemoEditorVisible) {
        MemoEditorDialog(
            memo = state.draft.decor.memo,
            onConfirm = { memo -> onIntent(CustomizeIntent.Decor.ChangeMemo(memo)) },
            onDismiss = { onIntent(CustomizeIntent.Decor.DismissMemoEditor) },
        )
    }
}

@Composable
private fun CustomizePermissionSheet(
    permission: PermissionValue?,
    onIntent: (CustomizeIntent) -> Unit,
) {
    val permissions = remember(permission) {
        permission?.let { persistentListOf(PermissionUiModel(permission = it, isGranted = false)) }
            ?: persistentListOf()
    }

    AppPermissionSheet(
        isVisible = permission != null,
        permissions = permissions,
        title = stringResource(R.string.customize_permission_title),
        subtitle = stringResource(R.string.customize_permission_desc),
        onAllowClick = { onIntent(CustomizeIntent.Permission.OpenPermissionSettings(it)) },
        onDismiss = { onIntent(CustomizeIntent.Permission.DismissPermissionSheet) },
    )
}

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun CustomizeContentPreview() {
    Theme {
        CustomizeContent(
            state = CustomizeState(
                isLoading = false,
                nowMillis = System.currentTimeMillis(),
                selectedTab = CustomizeTabValue.CLOCK,
            ),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun CustomizeContentGuidePreview() {
    Theme {
        CustomizeContent(
            state = CustomizeState(
                isLoading = false,
                nowMillis = System.currentTimeMillis(),
                guideStep = CustomizeGuideStepValue.INFO,
            ),
            onIntent = {},
        )
    }
}
