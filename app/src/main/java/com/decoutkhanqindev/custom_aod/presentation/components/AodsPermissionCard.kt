package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val isGranted = permission.status == PermissionStatusValue.GRANTED
    val borderColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "PermissionCardBorder",
    )

    val clickModifier = if (isGranted) {
        Modifier
    } else {
        Modifier
            .onClick(shape = MaterialTheme.shapes.large, action = onAllowClick)
            .semantics { role = Role.Button }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = permission.permission.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(permission.permission.nameRes),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )

            AnimatedVisibility(visible = permission.status == PermissionStatusValue.UNKNOWN) {
                Text(
                    text = stringResource(R.string.permission_unknown_hint),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        AnimatedContent(
            targetState = isGranted,
            modifier = Modifier
                .size(40.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.Center,
            label = "PermissionCardAction",
        ) { granted ->
            if (granted) {
                PermissionCardAction(
                    icon = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.permission_state_granted),
                )
            } else {
                PermissionCardAction(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = stringResource(R.string.action_allow),
                )
            }
        }
    }
}

@Composable
private fun PermissionCardAction(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
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
            permission = PermissionUiModel(
                permission = PermissionValue.NOTIFICATIONS,
                isGranted = true
            ),
            onAllowClick = {},
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsPermissionCardUnknownPreview() {
    AodsTheme {
        AodsPermissionCard(
            permission = PermissionUiModel(
                permission = PermissionValue.MIUI_LOCK_SCREEN,
                isGranted = null
            ),
            onAllowClick = {},
        )
    }
}
