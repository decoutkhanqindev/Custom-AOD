package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography

@Composable
fun MainNotificationsSection(
    options: AodNotificationOptionsUiModel,
    isAccessGranted: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AodsSectionHeader(title = stringResource(R.string.section_notifications))

        AnimatedVisibility(visible = !isAccessGranted) {
            Text(
                text = stringResource(R.string.notification_access_needed),
                modifier = Modifier.padding(vertical = 4.dp),
                color = AodsColors.Grey9A,
                style = AodsTypography.BodySmall,
            )
        }

        AodsSwitchRow(
            label = stringResource(R.string.opt_notification_icons),
            isChecked = options.isIconsEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleNotificationIcons(it)) },
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_notification_content),
            isChecked = options.isContentEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleNotificationContent(it)) },
            description = stringResource(R.string.opt_notification_content_desc),
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_edge_glow),
            isChecked = options.isEdgeGlowEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleEdgeGlow(it)) },
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_media_controls),
            isChecked = options.isMediaControlsEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleMediaControls(it)) },
        )
    }
}
