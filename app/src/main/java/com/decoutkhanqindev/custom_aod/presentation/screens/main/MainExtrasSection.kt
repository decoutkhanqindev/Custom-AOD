package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsValueRow
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainExtrasState
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainExtrasSection(
    extras: MainExtrasState,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AodsSectionHeader(title = stringResource(R.string.section_extras))

        AodsSwitchRow(
            label = stringResource(R.string.opt_date),
            isChecked = extras.settings.isDateEnabled,
            onCheckedChange = { onIntent(MainIntent.Extras.ToggleDate(it)) },
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_battery),
            isChecked = extras.settings.isBatteryEnabled,
            onCheckedChange = { onIntent(MainIntent.Extras.ToggleBattery(it)) },
        )

        AodsValueRow(
            label = stringResource(R.string.opt_memo),
            value = stringResource(if (extras.settings.memo.isBlank()) R.string.memo_add else R.string.memo_edit),
            onClick = { onIntent(MainIntent.Extras.ShowMemoEditor) },
            description = extras.settings.memo.ifBlank { null },
        )

        AodsValueRow(
            label = stringResource(R.string.opt_drawing),
            value = stringResource(if (extras.hasDrawing) R.string.drawing_redraw else R.string.drawing_draw),
            onClick = { onIntent(MainIntent.Extras.ShowDrawingPad) },
        )

        AnimatedVisibility(visible = extras.hasDrawing) {
            AodsValueRow(
                label = stringResource(R.string.drawing_remove),
                value = "",
                onClick = { onIntent(MainIntent.Extras.RemoveDrawing) },
            )
        }

        AodsSwitchRow(
            label = stringResource(R.string.opt_calendar),
            isChecked = extras.settings.isCalendarEnabled && extras.hasCalendarPermission,
            onCheckedChange = { onIntent(MainIntent.Extras.ToggleCalendar(it)) },
            description = stringResource(R.string.opt_calendar_desc),
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_weather),
            isChecked = extras.settings.isWeatherEnabled && extras.hasLocationPermission,
            onCheckedChange = { onIntent(MainIntent.Extras.ToggleWeather(it)) },
            description = stringResource(R.string.opt_weather_desc),
        )

        AnimatedVisibility(visible = extras.settings.isWeatherEnabled && extras.hasLocationPermission) {
            AodsSwitchRow(
                label = stringResource(R.string.opt_weather_fahrenheit),
                isChecked = extras.settings.isWeatherFahrenheit,
                onCheckedChange = { onIntent(MainIntent.Extras.ToggleWeatherFahrenheit(it)) },
            )
        }
    }

    if (extras.isMemoEditorVisible) {
        MemoEditorDialog(
            memo = extras.settings.memo,
            onConfirm = { memo -> onIntent(MainIntent.Extras.ChangeMemo(memo)) },
            onDismiss = { onIntent(MainIntent.Extras.DismissMemoEditor) },
        )
    }

    if (extras.isDrawingPadVisible) {
        DrawingPadDialog(
            onConfirm = { drawing -> onIntent(MainIntent.Extras.ChangeDrawing(drawing)) },
            onDismiss = { onIntent(MainIntent.Extras.DismissDrawingPad) },
        )
    }
}
