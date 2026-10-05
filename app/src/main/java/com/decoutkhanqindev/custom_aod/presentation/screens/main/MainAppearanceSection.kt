package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsRadioRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSectionLabel
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSliderRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.AodsValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFontValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainAppearanceSection(
    appearance: AodAppearanceUiModel,
    wallpaper: WallpaperValue?,
    hasBackground: Boolean,
    isSavingBackground: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AodsSectionHeader(title = stringResource(R.string.section_appearance))

        AodsSectionLabel(text = stringResource(R.string.opt_clock_face))

        ClockFaceValue.entries.forEach { face ->
            AodsRadioRow(
                label = stringResource(face.labelRes),
                isSelected = appearance.face == face,
                onClick = { onIntent(MainIntent.ChangeClockFace(face)) },
            )
        }

        AodsSectionLabel(text = stringResource(R.string.opt_clock_font))

        ClockFontValue.entries.forEach { font ->
            AodsRadioRow(
                label = stringResource(font.labelRes),
                isSelected = appearance.font == font,
                onClick = { onIntent(MainIntent.ChangeClockFont(font)) },
                labelFontFamily = font.fontFamily,
            )
        }

        AodsSectionLabel(text = stringResource(R.string.opt_clock_color))

        ClockColorPicker(
            selected = appearance.color,
            onSelect = { color -> onIntent(MainIntent.ChangeClockColor(color)) },
        )

        AodsSliderRow(
            label = stringResource(R.string.opt_clock_size_value, appearance.sizePercent),
            value = appearance.sizePercent,
            valueRange = AodAppearanceUiModel.SIZE_MIN_PERCENT..AodAppearanceUiModel.SIZE_MAX_PERCENT,
            step = AodAppearanceUiModel.SIZE_STEP_PERCENT,
            onValueChange = { onIntent(MainIntent.ChangeClockSize(it)) },
        )

        AodsSwitchRow(
            label = stringResource(R.string.opt_landscape),
            isChecked = appearance.isLandscape,
            onCheckedChange = { onIntent(MainIntent.ToggleLandscape(it)) },
        )

        AodsSectionLabel(text = stringResource(R.string.opt_background))

        WallpaperPicker(
            selected = wallpaper,
            onSelect = { selected -> onIntent(MainIntent.SelectWallpaper(selected)) },
        )

        AodsValueRow(
            label = stringResource(R.string.background_from_device),
            value = stringResource(
                when {
                    isSavingBackground -> R.string.background_saving
                    hasBackground -> R.string.background_change
                    else -> R.string.background_choose
                },
            ),
            onClick = { onIntent(MainIntent.OpenBackgroundPicker) },
        )

        AnimatedVisibility(visible = hasBackground || wallpaper != null) {
            AodsValueRow(
                label = stringResource(R.string.background_remove),
                value = "",
                onClick = { onIntent(MainIntent.RemoveBackground) },
            )
        }
    }
}
