package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionStatusValue
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
fun AodsPermissionCard(
    permission: PermissionUiModel,
    onAllowClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AodsTheme.colors.surfaceContainer,
) {
    val onboarding = AodsTheme.onboarding
    val borderColor by animateColorAsState(
        targetValue = if (permission.status == PermissionStatusValue.GRANTED) {
            AodsTheme.colors.primary
        } else {
            AodsTheme.colors.outlineVariant
        },
        label = "PermissionCardBorder",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(onboarding.cardShape)
            .background(containerColor)
            .border(width = onboarding.cardBorderWidth, color = borderColor, shape = onboarding.cardShape)
            .padding(onboarding.cardPadding),
    ) {
        Box(
            modifier = Modifier
                .size(onboarding.iconContainerSize)
                .background(
                    color = AodsTheme.colors.primary.copy(alpha = AodsTheme.opacity.tint),
                    shape = AodsTheme.shapes.full,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = permission.permission.icon,
                contentDescription = null,
                modifier = Modifier.size(onboarding.iconSize),
                tint = AodsTheme.colors.primary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = onboarding.contentGap),
        ) {
            Text(
                text = stringResource(permission.permission.nameRes),
                color = AodsTheme.colors.onSurface,
                style = AodsTheme.typography.titleMedium,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = AodsTheme.spacing.inlineGap),
                color = AodsTheme.colors.onSurfaceVariant,
                style = AodsTheme.typography.bodyMedium,
            )

            AnimatedContent(
                targetState = permission.status,
                modifier = Modifier
                    .padding(top = onboarding.actionTopPadding)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                label = "PermissionCardStatus",
            ) { status ->
                when (status) {
                    PermissionStatusValue.GRANTED -> PermissionGranted()
                    PermissionStatusValue.UNKNOWN -> PermissionUnknown(onOpenSettings = onAllowClick)
                    PermissionStatusValue.MISSING_REQUIRED,
                    PermissionStatusValue.MISSING_OPTIONAL -> OutlinedButton(onClick = onAllowClick) {
                        Text(text = stringResource(R.string.action_allow))
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionGranted() {
    Row(
        modifier = Modifier.heightIn(min = AodsTheme.onboarding.iconContainerSize),
        horizontalArrangement = Arrangement.spacedBy(AodsTheme.spacing.stackGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(AodsTheme.onboarding.iconSize),
            tint = AodsTheme.colors.primary,
        )

        Text(
            text = stringResource(R.string.permission_state_granted),
            color = AodsTheme.colors.primary,
            style = AodsTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun PermissionUnknown(onOpenSettings: () -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.permission_unknown_hint),
            color = AodsTheme.colors.onSurfaceVariant,
            style = AodsTheme.typography.bodySmall,
        )

        TextButton(onClick = onOpenSettings) {
            Text(text = stringResource(R.string.open_settings))
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsPermissionCardMissingPreview() {
    AodsTheme {
        AodsPermissionCard(
            permission = PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = false),
            onAllowClick = {},
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsPermissionCardGrantedPreview() {
    AodsTheme {
        AodsPermissionCard(
            permission = PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
            onAllowClick = {},
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsPermissionCardUnknownPreview() {
    AodsTheme {
        AodsPermissionCard(
            permission = PermissionUiModel(permission = PermissionValue.MIUI_LOCK_SCREEN, isGranted = null),
            onAllowClick = {},
        )
    }
}
