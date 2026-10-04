package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsRadioRow
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodInteractionUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainInteractionSection(
    interaction: AodInteractionUiModel,
    editingGesture: AodGestureValue?,
    isFlashlightAvailable: Boolean,
    isLightSensorAvailable: Boolean,
    isPickupSensorAvailable: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSectionHeader(title = stringResource(R.string.section_interaction))

        AodGestureValue.entries.forEach { gesture ->
            SettingsValueRow(
                label = stringResource(gesture.labelRes),
                value = stringResource(interaction.actionOf(gesture).labelRes),
                onClick = { onIntent(MainIntent.ShowGestureActionPicker(gesture)) },
            )
        }

        SettingsSwitchRow(
            label = stringResource(R.string.opt_auto_dim),
            isChecked = interaction.isAutoDimEnabled && isLightSensorAvailable,
            onCheckedChange = { onIntent(MainIntent.ToggleAutoDim(it)) },
            description = stringResource(
                if (isLightSensorAvailable) R.string.opt_auto_dim_desc else R.string.sensor_unsupported_light,
            ),
            isEnabled = isLightSensorAvailable,
        )

        SettingsSwitchRow(
            label = stringResource(R.string.opt_raise_to_wake),
            isChecked = interaction.isRaiseToWakeEnabled && isPickupSensorAvailable,
            onCheckedChange = { onIntent(MainIntent.ToggleRaiseToWake(it)) },
            description = stringResource(
                if (isPickupSensorAvailable) R.string.opt_raise_to_wake_desc else R.string.sensor_unsupported_pickup,
            ),
            isEnabled = isPickupSensorAvailable,
        )
    }

    editingGesture?.let { gesture ->
        GestureActionDialog(
            gesture = gesture,
            selected = interaction.actionOf(gesture),
            isFlashlightAvailable = isFlashlightAvailable,
            onSelect = { action -> onIntent(MainIntent.ChangeGestureAction(gesture, action)) },
            onDismiss = { onIntent(MainIntent.DismissGestureActionPicker) },
        )
    }
}

@Composable
private fun GestureActionDialog(
    gesture: AodGestureValue,
    selected: AodActionValue,
    isFlashlightAvailable: Boolean,
    onSelect: (AodActionValue) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
        title = { Text(text = stringResource(gesture.labelRes)) },
        text = {
            Column {
                AodActionValue.entries
                    .filter { action -> action != AodActionValue.FLASHLIGHT || isFlashlightAvailable }
                    .forEach { action ->
                        SettingsRadioRow(
                            label = stringResource(action.labelRes),
                            isSelected = action == selected,
                            onClick = { onSelect(action) },
                        )
                    }
            }
        },
    )
}
