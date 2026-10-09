package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography
import kotlin.math.roundToInt

@Composable
fun AodsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(
            top = 28.dp,
            bottom = 4.dp,
        ),
        color = AodsColors.Mint,
        style = AodsTypography.LabelMedium,
    )
}

@Composable
fun AodsSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(
            top = 12.dp,
            bottom = 4.dp,
        ),
        color = AodsColors.Grey9A,
        style = AodsTypography.BodyMedium,
    )
}

@Composable
fun AodsSwitchRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    isEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = isChecked, enabled = isEnabled, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .alpha(if (isEnabled) 1f else 0.38f),
        ) {
            Text(
                text = label,
                style = AodsTypography.BodyLarge,
            )

            description?.let { text ->
                Text(
                    text = text,
                    modifier = Modifier.padding(top = 2.dp),
                    color = AodsColors.Grey9A,
                    style = AodsTypography.BodySmall,
                )
            }
        }

        Switch(checked = isChecked, onCheckedChange = null, enabled = isEnabled)
    }
}

@Composable
fun AodsRadioRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelFontFamily: FontFamily? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)

        Text(
            text = label,
            modifier = Modifier.padding(start = 12.dp),
            fontFamily = labelFontFamily,
            style = AodsTypography.BodyLarge,
        )
    }
}

@Composable
fun AodsValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .onClick(shape = AodsShapes.RoundedCornerShape8dp, action = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
        ) {
            Text(
                text = label,
                style = AodsTypography.BodyLarge,
            )

            description?.let { text ->
                Text(
                    text = text,
                    modifier = Modifier.padding(top = 2.dp),
                    color = AodsColors.Grey9A,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AodsTypography.BodySmall,
                )
            }
        }

        Text(
            text = value,
            color = AodsColors.Mint,
            style = AodsTypography.BodyLarge,
        )
    }
}

@Composable
fun AodsSliderRow(
    label: String,
    value: Int,
    valueRange: IntRange,
    step: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stepCount = (valueRange.last - valueRange.first) / step

    Column(modifier = modifier.padding(top = 12.dp)) {
        Text(
            text = label,
            style = AodsTypography.BodyLarge,
        )

        Slider(
            value = ((value - valueRange.first) / step).toFloat(),
            onValueChange = { onValueChange(valueRange.first + it.roundToInt() * step) },
            valueRange = 0f..stepCount.toFloat(),
            steps = if (step == 1) 0 else stepCount - 1,
        )
    }
}
