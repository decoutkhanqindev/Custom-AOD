package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingTopBar
import com.decoutkhanqindev.custom_aod.presentation.components.AodsPermissionSheet
import com.decoutkhanqindev.custom_aod.presentation.components.onClick
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeClusterValue
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeDraftUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeGuideStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.model.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.AodContent
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeDecorState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeHistoryState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeState
import com.decoutkhanqindev.custom_aod.presentation.screens.main.DrawingPadDialog
import com.decoutkhanqindev.custom_aod.presentation.screens.main.MemoEditorDialog
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography
import kotlin.math.roundToInt
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CustomizeContent(
    state: CustomizeState,
    onIntent: (CustomizeIntent) -> Unit,
) {
    val preview = rememberCustomizePreview(state)
    val guideTargets = remember { mutableStateMapOf<CustomizeGuideStepValue, Rect>() }
    val onTargetPositioned = remember(guideTargets) {
        { step: CustomizeGuideStepValue, bounds: Rect -> if (guideTargets[step] != bounds) guideTargets[step] = bounds }
    }
    var lastGuideStep by remember { mutableStateOf(CustomizeGuideStepValue.PREVIEW) }

    SideEffect(state.guideStep) {
        state.guideStep?.let { step -> lastGuideStep = step }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AodsColors.Black),
    ) {
        AodContent(state = preview, onIntent = {})

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AodsOnboardingTopBar(
                step = OnboardingStepValue.CUSTOMIZE,
                onNavigateBack = { onIntent(CustomizeIntent.NavigateBack) },
                containerColor = Color.Transparent,
                actions = {
                    IconButton(onClick = { onIntent(CustomizeIntent.Guide.ShowGuide) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.customize_show_guide),
                            tint = AodsColors.Grey9A,
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
                    onTargetPositioned(CustomizeGuideStepValue.ACTIONS, coordinates.boundsInWindow())
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
private fun CustomizeToolbar(
    history: CustomizeHistoryState,
    isApplying: Boolean,
    onIntent: (CustomizeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = AodsColors.Neutral12,
        contentColor = AodsColors.GreyED,
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onIntent(CustomizeIntent.History.UndoChange) }, enabled = history.canUndo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = stringResource(R.string.customize_undo),
                )
            }

            IconButton(onClick = { onIntent(CustomizeIntent.History.RedoChange) }, enabled = history.canRedo) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = stringResource(R.string.customize_redo),
                )
            }

            IconButton(
                onClick = { onIntent(CustomizeIntent.History.ShowResetConfirm) },
                enabled = !history.isDefault,
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = stringResource(R.string.customize_reset),
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                color = AodsColors.NeutralVariant30,
            )

            Button(
                onClick = { onIntent(CustomizeIntent.ConfirmCustomization) },
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(min = 40.dp),
                enabled = history.hasChanges && !isApplying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AodsColors.Mint,
                    contentColor = AodsColors.Black,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Text(text = stringResource(R.string.customize_apply))
            }
        }
    }
}

@Composable
private fun CustomizePanelHost(
    selectedTab: CustomizeTabValue?,
    draft: CustomizeDraftUiModel,
    decor: CustomizeDecorState,
    onIntent: (CustomizeIntent) -> Unit,
) {
    var lastTab by remember { mutableStateOf(selectedTab ?: CustomizeTabValue.entries.first()) }

    SideEffect(selectedTab) {
        selectedTab?.let { tab -> lastTab = tab }
    }

    AnimatedVisibility(
        visible = selectedTab != null,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = 0.9f,
                stiffness = 700f,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            initialOffsetY = { height -> height },
        ),
        exit = slideOutVertically(
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = 3800f,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            targetOffsetY = { height -> height },
        ),
        label = "CustomizePanel",
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AodsShapes.RoundedCornerTopShape16dp,
            color = AodsColors.Neutral12,
            contentColor = AodsColors.GreyED,
        ) {
            AnimatedContent(
                targetState = selectedTab ?: lastTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "CustomizePanelContent",
            ) { tab ->
                Column(modifier = Modifier.heightIn(max = 360.dp)) {
                    Text(
                        text = stringResource(tab.labelRes),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center,
                        style = AodsTypography.TitleMedium,
                    )

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                    ) {
                        CustomizePanel(
                            tab = tab,
                            draft = draft,
                            decor = decor,
                            onIntent = onIntent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizeTabBar(
    selectedTab: CustomizeTabValue?,
    guideStep: CustomizeGuideStepValue?,
    onIntent: (CustomizeIntent) -> Unit,
    onClusterPositioned: (CustomizeGuideStepValue, Rect) -> Unit,
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val clusterStarts = remember { mutableMapOf<CustomizeClusterValue, Float>() }
    val viewportStart = remember { floatArrayOf(0f) }

    LaunchedEffect(guideStep) {
        if (guideStep == CustomizeGuideStepValue.entries.first()) {
            scrollState.animateScrollTo(0)
            return@LaunchedEffect
        }
        val start = guideStep?.cluster?.let { cluster -> clusterStarts[cluster] } ?: return@LaunchedEffect
        val padding = with(density) { 8.dp.toPx() }
        scrollState.animateScrollTo((scrollState.value + start - viewportStart[0] - padding).roundToInt().coerceAtLeast(0))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AodsColors.Neutral12,
        contentColor = AodsColors.Grey9A,
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .onGloballyPositioned { coordinates -> viewportStart[0] = coordinates.positionInWindow().x }
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomizeClusterValue.entries.forEachIndexed { index, cluster ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 4.dp),
                        color = AodsColors.NeutralVariant30,
                    )
                }

                val step = CustomizeGuideStepValue.entries.first { it.cluster == cluster }
                Row(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInWindow()
                        clusterStarts[cluster] = position.x
                        onClusterPositioned(step, Rect(position, coordinates.size.toSize()))
                    },
                ) {
                    CustomizeTabValue.entries.filter { it.cluster == cluster }.forEach { tab ->
                        CustomizeTabItem(
                            tab = tab,
                            isSelected = tab == selectedTab,
                            onClick = { onIntent(CustomizeIntent.Panel.SelectTab(tab)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizeTabItem(
    tab: CustomizeTabValue,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) AodsColors.Mint else AodsColors.Grey9A,
        label = "CustomizeTabContent",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) AodsColors.MintAlpha12 else Color.Transparent,
        label = "CustomizeTabContainer",
    )

    Column(
        modifier = Modifier
            .widthIn(min = 64.dp)
            .onClick(shape = AodsShapes.RoundedCornerShape12dp, action = onClick)
            .drawBehind {
                drawRoundRect(color = containerColor, cornerRadius = CornerRadius(12.dp.toPx()))
            }
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )

        Text(
            text = stringResource(tab.labelRes),
            modifier = Modifier.padding(top = 4.dp),
            color = contentColor,
            maxLines = 1,
            style = AodsTypography.LabelMedium,
        )
    }
}

@Composable
private fun CustomizePermissionSheet(
    permission: PermissionValue?,
    onIntent: (CustomizeIntent) -> Unit,
) {
    val permissions = remember(permission) {
        permission?.let { persistentListOf(PermissionUiModel(permission = it, isGranted = false)) } ?: persistentListOf()
    }

    AodsPermissionSheet(
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
    AodsTheme {
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
    AodsTheme {
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
