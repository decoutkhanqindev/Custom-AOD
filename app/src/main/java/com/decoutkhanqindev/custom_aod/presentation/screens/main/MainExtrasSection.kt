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
import com.decoutkhanqindev.custom_aod.presentation.model.AodExtrasUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainExtrasSection(
    extras: AodExtrasUiModel,
    hasDrawing: Boolean,
    isEditingMemo: Boolean,
    isDrawingPadVisible: Boolean,
    hasCalendarPermission: Boolean,
    hasLocationPermission: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AodsSectionHeader(title = stringResource(R.string.section_extras))

        AodsValueRow(
            label = stringResource(R.string.opt_memo),
            value = stringResource(if (extras.memo.isBlank()) R.string.memo_add else R.string.memo_edit),
            onClick = { onIntent(MainIntent.ShowMemoEditor) },
            description = extras.memo.ifBlank { null },
        )

        AodsValueRow(
            label = stringResource(R.string.opt_drawing),
            value = stringResource(if (hasDrawing) R.string.drawing_redraw else R.string.drawing_draw),
            onClick = { onIntent(MainIntent.ShowDrawingPad) },
        )

        AnimatedVisibility(visible = hasDrawing) {
            AodsValueRow(
                label = stringResource(R.string.drawing_remove),
                value = "",
                onClick = { onIntent(MainIntent.RemoveDrawing) },
            )
        }

        AodsSwitchRow(
            label = stringResource(R.string.opt_calendar),
            isChecked = extras.isCalendarEnabled && hasCalendarPermission,
            onCheckedChange = { onIntent(MainIntent.ToggleCalendar(it)) },
            description = stringResource(R.string.opt_calendar_desc),
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_weather),
            isChecked = extras.isWeatherEnabled && hasLocationPermission,
            onCheckedChange = { onIntent(MainIntent.ToggleWeather(it)) },
            description = stringResource(R.string.opt_weather_desc),
        )

        AnimatedVisibility(visible = extras.isWeatherEnabled && hasLocationPermission) {
            AodsSwitchRow(
                label = stringResource(R.string.opt_weather_fahrenheit),
                isChecked = extras.isWeatherFahrenheit,
                onCheckedChange = { onIntent(MainIntent.ToggleWeatherFahrenheit(it)) },
            )
        }
    }

    if (isEditingMemo) {
        MemoEditorDialog(
            memo = extras.memo,
            onConfirm = { memo -> onIntent(MainIntent.ChangeMemo(memo)) },
            onDismiss = { onIntent(MainIntent.DismissMemoEditor) },
        )
    }

    if (isDrawingPadVisible) {
        DrawingPadDialog(
            onConfirm = { drawing -> onIntent(MainIntent.ChangeDrawing(drawing)) },
            onDismiss = { onIntent(MainIntent.DismissDrawingPad) },
        )
    }
}
