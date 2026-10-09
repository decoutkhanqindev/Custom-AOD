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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography

@Composable
internal fun WallpaperPicker(
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
                    .clip(AodsShapes.RoundedCornerShape8dp)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(wallpaper) })
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 132.dp)
                        .clip(AodsShapes.RoundedCornerShape12dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                AodsColors.Mint
                            } else {
                                AodsColors.NeutralVariant30
                            },
                            shape = AodsShapes.RoundedCornerShape12dp,
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
                            tint = AodsColors.Mint,
                        )
                    }
                }

                Text(
                    text = stringResource(wallpaper.labelRes),
                    modifier = Modifier.padding(top = 6.dp),
                    color = if (isSelected) {
                        AodsColors.Mint
                    } else {
                        AodsColors.Grey9A
                    },
                    style = AodsTypography.LabelMedium,
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
                            color = if (isSelected) AodsColors.GreyED else color.color,
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
                            tint = AodsColors.Black,
                        )
                    }
                }
            }
        }
    }
}
