package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsLabel
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsRadioRow
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSectionHeader
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSlider
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsSwitchRow
import com.decoutkhanqindev.custom_aod.presentation.components.SettingsValueRow
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
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
        SettingsSectionHeader(title = stringResource(R.string.section_appearance))

        SettingsLabel(text = stringResource(R.string.opt_clock_face))

        ClockFaceValue.entries.forEach { face ->
            SettingsRadioRow(
                label = stringResource(face.labelRes),
                isSelected = appearance.face == face,
                onClick = { onIntent(MainIntent.ChangeClockFace(face)) },
            )
        }

        SettingsLabel(text = stringResource(R.string.opt_clock_font))

        ClockFontValue.entries.forEach { font ->
            SettingsRadioRow(
                label = stringResource(font.labelRes),
                isSelected = appearance.font == font,
                onClick = { onIntent(MainIntent.ChangeClockFont(font)) },
                labelFontFamily = font.fontFamily,
            )
        }

        SettingsLabel(text = stringResource(R.string.opt_clock_color))

        ClockColorPicker(
            selected = appearance.color,
            onSelect = { color -> onIntent(MainIntent.ChangeClockColor(color)) },
        )

        SettingsSlider(
            label = stringResource(R.string.opt_clock_size_value, appearance.sizePercent),
            value = appearance.sizePercent,
            valueRange = AodAppearanceUiModel.SIZE_MIN_PERCENT..AodAppearanceUiModel.SIZE_MAX_PERCENT,
            step = AodAppearanceUiModel.SIZE_STEP_PERCENT,
            onValueChange = { onIntent(MainIntent.ChangeClockSize(it)) },
        )

        SettingsSwitchRow(
            label = stringResource(R.string.opt_landscape),
            isChecked = appearance.isLandscape,
            onCheckedChange = { onIntent(MainIntent.ToggleLandscape(it)) },
        )

        SettingsLabel(text = stringResource(R.string.opt_background))

        WallpaperPicker(
            selected = wallpaper,
            onSelect = { selected -> onIntent(MainIntent.SelectWallpaper(selected)) },
        )

        SettingsValueRow(
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
            SettingsValueRow(
                label = stringResource(R.string.background_remove),
                value = "",
                onClick = { onIntent(MainIntent.RemoveBackground) },
            )
        }
    }
}

@Composable
private fun WallpaperPicker(
    selected: WallpaperValue?,
    onSelect: (WallpaperValue) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WallpaperValue.entries.forEach { wallpaper ->
            val isSelected = wallpaper == selected

            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(wallpaper) })
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 132.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = MaterialTheme.shapes.medium,
                        ),
                ) {
                    Image(
                        painter = painterResource(wallpaper.drawableRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Text(
                    text = stringResource(wallpaper.labelRes),
                    modifier = Modifier.padding(top = 6.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ClockColorPicker(
    selected: ClockColorValue,
    onSelect: (ClockColorValue) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        ClockColorValue.entries.forEach { color ->
            val label = stringResource(color.labelRes)
            val isSelected = color == selected

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.color)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else color.color,
                            shape = CircleShape,
                        )
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(color) })
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
                }
            }
        }
    }
}
