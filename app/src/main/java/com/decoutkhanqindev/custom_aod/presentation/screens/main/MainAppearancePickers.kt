package com.decoutkhanqindev.custom_aod.presentation.screens.main

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
internal fun WallpaperPicker(
    selected: WallpaperValue?,
    onSelect: (WallpaperValue) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = AodsTheme.picker.verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(AodsTheme.picker.thumbnailGap),
    ) {
        WallpaperValue.entries.forEach { wallpaper ->
            val isSelected = wallpaper == selected

            Column(
                modifier = Modifier
                    .clip(AodsTheme.shapes.small)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(wallpaper) })
                    .padding(AodsTheme.picker.thumbnailItemPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = AodsTheme.picker.thumbnailWidth, height = AodsTheme.picker.thumbnailHeight)
                        .clip(AodsTheme.shapes.medium)
                        .border(
                            width = if (isSelected) AodsTheme.picker.thumbnailSelectedBorderWidth else AodsTheme.picker.thumbnailBorderWidth,
                            color = if (isSelected) {
                                AodsTheme.colors.primary
                            } else {
                                AodsTheme.colors.outlineVariant
                            },
                            shape = AodsTheme.shapes.medium,
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
                                .padding(AodsTheme.picker.thumbnailCheckPadding)
                                .size(AodsTheme.picker.thumbnailCheckSize),
                            tint = AodsTheme.colors.primary,
                        )
                    }
                }

                Text(
                    text = stringResource(wallpaper.labelRes),
                    modifier = Modifier.padding(top = AodsTheme.picker.thumbnailLabelTopPadding),
                    color = if (isSelected) {
                        AodsTheme.colors.primary
                    } else {
                        AodsTheme.colors.onSurfaceVariant
                    },
                    style = AodsTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
internal fun ClockColorPicker(
    selected: ClockColorValue,
    onSelect: (ClockColorValue) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AodsTheme.picker.verticalPadding),
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
                        .size(AodsTheme.picker.swatchSize)
                        .clip(AodsTheme.shapes.full)
                        .background(color.color)
                        .border(
                            width = AodsTheme.picker.swatchBorderWidth,
                            color = if (isSelected) AodsTheme.colors.onBackground else color.color,
                            shape = AodsTheme.shapes.full,
                        )
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(color) })
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(AodsTheme.picker.swatchCheckSize),
                            tint = AodsTheme.colors.background,
                        )
                    }
                }
            }
        }
    }
}
