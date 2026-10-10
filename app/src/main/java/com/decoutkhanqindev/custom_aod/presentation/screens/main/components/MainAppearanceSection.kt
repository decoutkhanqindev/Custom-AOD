package com.decoutkhanqindev.custom_aod.presentation.screens.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.aod.ClockColorPicker
import com.decoutkhanqindev.custom_aod.presentation.components.aod.WallpaperPicker
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppRadioRow
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSectionLabel
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSliderRow
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.settings.AppValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.ClockFontValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainAppearanceState
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent

@Composable
fun MainAppearanceSection(
    appearance: MainAppearanceState,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppSectionHeader(title = stringResource(R.string.section_appearance))

        AppSectionLabel(text = stringResource(R.string.opt_clock_face))

        ClockFaceValue.entries.forEach { face ->
            AppRadioRow(
                label = stringResource(face.labelRes),
                isSelected = appearance.settings.face == face,
                onClick = { onIntent(MainIntent.Appearance.ChangeClockFace(face)) },
            )
        }

        AppSectionLabel(text = stringResource(R.string.opt_clock_font))

        ClockFontValue.entries.forEach { font ->
            AppRadioRow(
                label = stringResource(font.labelRes),
                isSelected = appearance.settings.font == font,
                onClick = { onIntent(MainIntent.Appearance.ChangeClockFont(font)) },
                labelFontFamily = font.fontFamily,
            )
        }

        AppSectionLabel(text = stringResource(R.string.opt_clock_color))

        ClockColorPicker(
            selected = appearance.settings.color,
            onSelect = { color -> onIntent(MainIntent.Appearance.ChangeClockColor(color)) },
        )

        AppSliderRow(
            label = stringResource(R.string.opt_clock_size_value, appearance.settings.sizePercent),
            value = appearance.settings.sizePercent,
            valueRange = AodAppearanceUiModel.SIZE_MIN_PERCENT..AodAppearanceUiModel.SIZE_MAX_PERCENT,
            step = AodAppearanceUiModel.SIZE_STEP_PERCENT,
            onValueChange = { onIntent(MainIntent.Appearance.ChangeClockSize(it)) },
        )

        AppSwitchRow(
            label = stringResource(R.string.opt_landscape),
            isChecked = appearance.settings.isLandscape,
            onCheckedChange = { onIntent(MainIntent.Appearance.ToggleLandscape(it)) },
        )

        AppSectionLabel(text = stringResource(R.string.opt_background))

        WallpaperPicker(
            selected = appearance.wallpaper,
            onSelect = { selected -> onIntent(MainIntent.Appearance.SelectWallpaper(selected)) },
        )

        AppValueRow(
            label = stringResource(R.string.background_from_device),
            value = stringResource(
                when {
                    appearance.isSavingBackground -> R.string.background_saving
                    appearance.hasBackground -> R.string.background_change
                    else -> R.string.background_choose
                },
            ),
            onClick = { onIntent(MainIntent.Appearance.OpenBackgroundPicker) },
        )

        AnimatedVisibility(visible = appearance.hasBackground || appearance.wallpaper != null) {
            AppValueRow(
                label = stringResource(R.string.background_remove),
                value = "",
                onClick = { onIntent(MainIntent.Appearance.RemoveBackground) },
            )
        }
    }
}
