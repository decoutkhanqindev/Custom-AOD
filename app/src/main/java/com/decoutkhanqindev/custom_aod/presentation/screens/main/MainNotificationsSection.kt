package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainNotificationsSection(
    options: AodNotificationOptionsUiModel,
    isAccessGranted: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(title = stringResource(R.string.section_notifications))

        AnimatedVisibility(visible = !isAccessGranted) {
            Text(
                text = stringResource(R.string.notification_access_needed),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        SettingsSwitchRow(
            label = stringResource(R.string.opt_notification_icons),
            isChecked = options.isIconsEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleNotificationIcons(it)) },
        )

        SettingsSwitchRow(
            label = stringResource(R.string.opt_edge_glow),
            isChecked = options.isEdgeGlowEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleEdgeGlow(it)) },
        )

        SettingsSwitchRow(
            label = stringResource(R.string.opt_media_controls),
            isChecked = options.isMediaControlsEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleMediaControls(it)) },
        )
    }
}
