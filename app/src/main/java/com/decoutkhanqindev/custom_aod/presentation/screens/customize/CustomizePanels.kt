package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionLabel
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSliderRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFontValue
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeClusterValue
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeDecorUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeDraftUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeEffectsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeInfoUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeDecorState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.ClockColorPicker
import com.decoutkhanqindev.custom_aod.presentation.screens.main.WallpaperPicker

@Composable
internal fun CustomizePanel(
    tab: CustomizeTabValue,
    draft: CustomizeDraftUiModel,
    decor: CustomizeDecorState,
    onIntent: (CustomizeIntent) -> Unit,
) {
    when (tab.cluster) {
        CustomizeClusterValue.APPEARANCE -> AppearancePanel(tab = tab, appearance = draft.appearance, onIntent = onIntent)
        CustomizeClusterValue.DECOR -> DecorPanel(tab = tab, decor = draft.decor, decorState = decor, onIntent = onIntent)
        CustomizeClusterValue.INFO -> InfoPanel(tab = tab, info = draft.info, onIntent = onIntent)
        CustomizeClusterValue.EFFECT -> EffectsPanel(effects = draft.effects, onIntent = onIntent)
    }
}

@Composable
private fun AppearancePanel(
    tab: CustomizeTabValue,
    appearance: CustomizeAppearanceUiModel,
    onIntent: (CustomizeIntent) -> Unit,
) {
    when (tab) {
        CustomizeTabValue.DATE -> AodsSwitchRow(
            label = stringResource(R.string.opt_date),
            isChecked = appearance.isDateEnabled,
            onCheckedChange = { onIntent(CustomizeIntent.Appearance.ToggleDate(it)) },
        )

        CustomizeTabValue.BATTERY -> AodsSwitchRow(
            label = stringResource(R.string.opt_battery),
            isChecked = appearance.isBatteryEnabled,
            onCheckedChange = { onIntent(CustomizeIntent.Appearance.ToggleBattery(it)) },
        )

        else -> ClockPanel(clock = appearance.clock, onIntent = onIntent)
    }
}

@Composable
private fun DecorPanel(
    tab: CustomizeTabValue,
    decor: CustomizeDecorUiModel,
    decorState: CustomizeDecorState,
    onIntent: (CustomizeIntent) -> Unit,
) {
    when (tab) {
        CustomizeTabValue.DRAWING -> DrawingPanel(hasDrawing = decor.drawing != null, onIntent = onIntent)
        CustomizeTabValue.MEMO -> MemoPanel(memo = decor.memo, onIntent = onIntent)
        else -> BackgroundPanel(decor = decor, isLoadingBackground = decorState.isLoadingBackground, onIntent = onIntent)
    }
}

@Composable
private fun InfoPanel(
    tab: CustomizeTabValue,
    info: CustomizeInfoUiModel,
    onIntent: (CustomizeIntent) -> Unit,
) {
    when (tab) {
        CustomizeTabValue.EVENTS -> AodsSwitchRow(
            label = stringResource(R.string.opt_calendar),
            isChecked = info.isCalendarEnabled,
            onCheckedChange = { onIntent(CustomizeIntent.Info.ToggleCalendar(it)) },
            description = stringResource(R.string.opt_calendar_desc),
        )

        CustomizeTabValue.WEATHER -> Column {
            AodsSwitchRow(
                label = stringResource(R.string.opt_weather),
                isChecked = info.isWeatherEnabled,
                onCheckedChange = { onIntent(CustomizeIntent.Info.ToggleWeather(it)) },
                description = stringResource(R.string.opt_weather_desc),
            )

            AnimatedVisibility(visible = info.isWeatherEnabled) {
                AodsSwitchRow(
                    label = stringResource(R.string.opt_weather_fahrenheit),
                    isChecked = info.isWeatherFahrenheit,
                    onCheckedChange = { onIntent(CustomizeIntent.Info.ToggleWeatherFahrenheit(it)) },
                )
            }
        }

        CustomizeTabValue.MEDIA -> AodsSwitchRow(
            label = stringResource(R.string.opt_media_controls),
            isChecked = info.isMediaControlsEnabled,
            onCheckedChange = { onIntent(CustomizeIntent.Info.ToggleMediaControls(it)) },
        )

        else -> Column {
            AodsSwitchRow(
                label = stringResource(R.string.opt_notification_icons),
                isChecked = info.isNotificationIconsEnabled,
                onCheckedChange = { onIntent(CustomizeIntent.Info.ToggleNotificationIcons(it)) },
            )

            AodsSwitchRow(
                label = stringResource(R.string.opt_notification_content),
                isChecked = info.isNotificationContentEnabled,
                onCheckedChange = { onIntent(CustomizeIntent.Info.ToggleNotificationContent(it)) },
                description = stringResource(R.string.opt_notification_content_desc),
            )
        }
    }
}

@Composable
private fun EffectsPanel(
    effects: CustomizeEffectsUiModel,
    onIntent: (CustomizeIntent) -> Unit,
) {
    Column {
        AodsSwitchRow(
            label = stringResource(R.string.opt_edge_glow),
            isChecked = effects.isEdgeGlowEnabled,
            onCheckedChange = { onIntent(CustomizeIntent.Effects.ToggleEdgeGlow(it)) },
        )

        AodsValueRow(
            label = stringResource(R.string.customize_glow_preview),
            value = stringResource(R.string.action_preview),
            onClick = { onIntent(CustomizeIntent.Effects.PreviewEdgeGlow) },
        )
    }
}

@Composable
private fun ClockPanel(
    clock: AodAppearanceUiModel,
    onIntent: (CustomizeIntent) -> Unit,
) {
    Column {
        AodsSectionLabel(text = stringResource(R.string.opt_clock_face))

        ChipRow {
            ClockFaceValue.entries.forEach { face ->
                FilterChip(
                    selected = clock.face == face,
                    onClick = { onIntent(CustomizeIntent.Appearance.ChangeClockFace(face)) },
                    label = { Text(text = stringResource(face.labelRes)) },
                )
            }
        }

        AodsSectionLabel(text = stringResource(R.string.opt_clock_font))

        ChipRow {
            ClockFontValue.entries.forEach { font ->
                FilterChip(
                    selected = clock.font == font,
                    onClick = { onIntent(CustomizeIntent.Appearance.ChangeClockFont(font)) },
                    label = { Text(text = stringResource(font.labelRes), fontFamily = font.fontFamily) },
                )
            }
        }

        AodsSectionLabel(text = stringResource(R.string.opt_clock_color))

        ClockColorPicker(
            selected = clock.color,
            onSelect = { color -> onIntent(CustomizeIntent.Appearance.ChangeClockColor(color)) },
        )

        AodsSliderRow(
            label = stringResource(R.string.opt_clock_size_value, clock.sizePercent),
            value = clock.sizePercent,
            valueRange = AodAppearanceUiModel.SIZE_MIN_PERCENT..AodAppearanceUiModel.SIZE_MAX_PERCENT,
            step = AodAppearanceUiModel.SIZE_STEP_PERCENT,
            onValueChange = { onIntent(CustomizeIntent.Appearance.ChangeClockSize(it)) },
        )
    }
}

@Composable
private fun BackgroundPanel(
    decor: CustomizeDecorUiModel,
    isLoadingBackground: Boolean,
    onIntent: (CustomizeIntent) -> Unit,
) {
    Column {
        WallpaperPicker(
            selected = decor.wallpaper,
            onSelect = { wallpaper -> onIntent(CustomizeIntent.Decor.SelectWallpaper(wallpaper)) },
        )

        AodsValueRow(
            label = stringResource(R.string.background_from_device),
            value = stringResource(
                when {
                    isLoadingBackground -> R.string.customize_background_loading
                    decor.background != null -> R.string.background_change
                    else -> R.string.background_choose
                },
            ),
            onClick = { onIntent(CustomizeIntent.Decor.OpenBackgroundPicker) },
        )

        AnimatedVisibility(visible = decor.background != null || decor.wallpaper != null) {
            AodsValueRow(
                label = stringResource(R.string.background_remove),
                value = "",
                onClick = { onIntent(CustomizeIntent.Decor.RemoveBackground) },
            )
        }
    }
}

@Composable
private fun DrawingPanel(
    hasDrawing: Boolean,
    onIntent: (CustomizeIntent) -> Unit,
) {
    Column {
        AodsValueRow(
            label = stringResource(R.string.opt_drawing),
            value = stringResource(if (hasDrawing) R.string.drawing_redraw else R.string.drawing_draw),
            onClick = { onIntent(CustomizeIntent.Decor.ShowDrawingPad) },
        )

        AnimatedVisibility(visible = hasDrawing) {
            AodsValueRow(
                label = stringResource(R.string.drawing_remove),
                value = "",
                onClick = { onIntent(CustomizeIntent.Decor.RemoveDrawing) },
            )
        }
    }
}

@Composable
private fun MemoPanel(
    memo: String,
    onIntent: (CustomizeIntent) -> Unit,
) {
    Column {
        AodsValueRow(
            label = stringResource(R.string.opt_memo),
            value = stringResource(if (memo.isBlank()) R.string.memo_add else R.string.memo_edit),
            onClick = { onIntent(CustomizeIntent.Decor.ShowMemoEditor) },
            description = memo.ifBlank { null },
        )

        AnimatedVisibility(visible = memo.isNotBlank()) {
            AodsValueRow(
                label = stringResource(R.string.customize_memo_remove),
                value = "",
                onClick = { onIntent(CustomizeIntent.Decor.ChangeMemo("")) },
            )
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}
