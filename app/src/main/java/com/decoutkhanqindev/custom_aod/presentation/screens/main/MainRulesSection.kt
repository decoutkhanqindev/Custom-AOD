package com.decoutkhanqindev.custom_aod.presentation.screens.main

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsRadioRow
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSlider
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodRulesUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodScheduleUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ChargingRuleValue
import com.decoutkhanqindev.custom_aod.presentation.model.ScheduleTimeValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun MainRulesSection(
    rules: AodRulesUiModel,
    editingScheduleTime: ScheduleTimeValue?,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = rememberTimeFormatter()

    Column(modifier = modifier) {
        SettingsSectionHeader(title = stringResource(R.string.section_rules))

        Text(
            text = stringResource(R.string.opt_charging_rule),
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        ChargingRuleValue.entries.forEach { rule ->
            SettingsRadioRow(
                label = stringResource(rule.labelRes),
                isSelected = rules.chargingRule == rule,
                onClick = { onIntent(MainIntent.ChangeChargingRule(rule)) },
            )
        }

        SettingsSwitchRow(
            label = stringResource(R.string.opt_schedule),
            isChecked = rules.schedule.isEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleSchedule(it)) },
        )

        AnimatedVisibility(visible = rules.schedule.isEnabled) {
            Column {
                ScheduleTimeValue.entries.forEach { time ->
                    SettingsValueRow(
                        label = stringResource(time.labelRes),
                        value = timeFormatter.format(AodScheduleUiModel.timeOf(rules.schedule.minuteOf(time))),
                        onClick = { onIntent(MainIntent.ShowScheduleTimePicker(time)) },
                    )
                }
            }
        }

        SettingsSlider(
            label = if (rules.minBattery == 0) {
                stringResource(R.string.opt_battery_off)
            } else {
                stringResource(R.string.opt_battery_value, rules.minBattery)
            },
            value = rules.minBattery,
            valueRange = 0..AodRulesUiModel.BATTERY_MAX_PERCENT,
            step = AodRulesUiModel.BATTERY_STEP_PERCENT,
            onValueChange = { onIntent(MainIntent.ChangeMinBattery(it)) },
        )
    }

    editingScheduleTime?.let { time ->
        ScheduleTimeDialog(
            time = time,
            initialTime = AodScheduleUiModel.timeOf(rules.schedule.minuteOf(time)),
            onConfirm = { picked -> onIntent(MainIntent.ChangeScheduleTime(time, AodScheduleUiModel.minuteOfDay(picked))) },
            onDismiss = { onIntent(MainIntent.DismissScheduleTimePicker) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTimeDialog(
    time: ScheduleTimeValue,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(text = stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
        title = { Text(text = stringResource(time.labelRes)) },
        text = { TimePicker(state = pickerState) },
    )
}

@Composable
private fun rememberTimeFormatter(): DateTimeFormatter {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val skeleton = stringResource(
        if (DateFormat.is24HourFormat(context)) R.string.time_skeleton_24h else R.string.time_skeleton_12h,
    )

    return remember(skeleton, locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    }
}
