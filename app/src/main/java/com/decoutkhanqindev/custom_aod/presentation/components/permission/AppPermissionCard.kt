package com.decoutkhanqindev.custom_aod.presentation.components.permission

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
import com.decoutkhanqindev.custom_aod.presentation.components.onClick
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionStatusValue
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.BodySmall
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey9A
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyED
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import com.decoutkhanqindev.custom_aod.presentation.theme.MintAlpha12
import com.decoutkhanqindev.custom_aod.presentation.theme.Neutral12
import com.decoutkhanqindev.custom_aod.presentation.theme.NeutralVariant30
import com.decoutkhanqindev.custom_aod.presentation.theme.RoundedCornerShape16dp
import com.decoutkhanqindev.custom_aod.presentation.theme.Theme
import com.decoutkhanqindev.custom_aod.presentation.theme.TitleMedium

@Composable
fun AppPermissionCard(
    permission: PermissionUiModel,
    onAllowClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Neutral12,
) {
    val isGranted = permission.status == PermissionStatusValue.GRANTED
    val borderColor by animateColorAsState(
        targetValue = if (isGranted) Mint else NeutralVariant30,
        label = "AppPermissionCardBorder",
    )

    val clickModifier = if (isGranted) {
        Modifier
    } else {
        Modifier
            .onClick(shape = RoundedCornerShape16dp, action = onAllowClick)
            .semantics { role = Role.Button }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .clip(RoundedCornerShape16dp)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape16dp
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MintAlpha12,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = permission.permission.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Mint,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(permission.permission.nameRes),
                color = GreyED,
                style = TitleMedium,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = 2.dp),
                color = Grey9A,
                style = BodySmall,
            )

            AnimatedVisibility(visible = permission.status == PermissionStatusValue.UNKNOWN) {
                Text(
                    text = stringResource(R.string.permission_unknown_hint),
                    modifier = Modifier.padding(top = 4.dp),
                    color = Mint,
                    style = BodySmall,
                )
            }
        }

        AnimatedContent(
            targetState = isGranted,
            modifier = Modifier
                .size(40.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.Center,
            label = "AppPermissionCardAction",
        ) { granted ->
            if (granted) {
                AppPermissionCardAction(
                    icon = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.permission_state_granted),
                )
            } else {
                AppPermissionCardAction(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = stringResource(R.string.action_allow),
                )
            }
        }
    }
}

@Composable
private fun AppPermissionCardAction(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Mint, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = Black,
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AppPermissionCardMissingPreview() {
    Theme {
        AppPermissionCard(
            permission = PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = false),
            onAllowClick = {},
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AppPermissionCardGrantedPreview() {
    Theme {
        AppPermissionCard(
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
private fun AppPermissionCardUnknownPreview() {
    Theme {
        AppPermissionCard(
            permission = PermissionUiModel(
                permission = PermissionValue.MIUI_LOCK_SCREEN,
                isGranted = null
            ),
            onAllowClick = {},
        )
    }
}
