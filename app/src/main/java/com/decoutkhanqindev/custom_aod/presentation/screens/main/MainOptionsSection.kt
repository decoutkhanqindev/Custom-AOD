package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSliderRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainOptionsSection(
    options: AodOptionsUiModel,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AodsSectionHeader(title = stringResource(R.string.section_options))

        AodsSwitchRow(
            label = stringResource(R.string.opt_custom_brightness),
            isChecked = options.isCustomBrightness,
            onCheckedChange = { onIntent(MainIntent.Options.ToggleCustomBrightness(it)) },
        )

        AnimatedVisibility(visible = options.isCustomBrightness) {
            AodsSliderRow(
                label = stringResource(R.string.opt_brightness_value, options.brightnessPercent),
                value = options.brightnessPercent,
                valueRange = AodOptionsUiModel.BRIGHTNESS_MIN_PERCENT..AodOptionsUiModel.BRIGHTNESS_MAX_PERCENT,
                step = 1,
                onValueChange = { onIntent(MainIntent.Options.ChangeBrightness(it)) },
            )
        }

        AodsSwitchRow(
            label = stringResource(R.string.opt_proximity),
            isChecked = options.isProximityEnabled,
            onCheckedChange = { onIntent(MainIntent.Options.ToggleProximity(it)) },
        )

        AodsSliderRow(
            label = if (options.timeoutMinutes == 0) {
                stringResource(R.string.opt_timeout_never)
            } else {
                stringResource(R.string.opt_timeout_value, options.timeoutMinutes)
            },
            value = options.timeoutMinutes,
            valueRange = 0..AodOptionsUiModel.TIMEOUT_MAX_MINUTES,
            step = AodOptionsUiModel.TIMEOUT_STEP_MINUTES,
            onValueChange = { onIntent(MainIntent.Options.ChangeTimeout(it)) },
        )
    }
}
