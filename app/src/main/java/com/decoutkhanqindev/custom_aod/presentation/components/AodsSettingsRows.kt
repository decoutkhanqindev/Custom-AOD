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
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import kotlin.math.roundToInt

@Composable
fun AodsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(
            top = AodsTheme.settingsRow.sectionHeaderTopPadding,
            bottom = AodsTheme.settingsRow.sectionHeaderBottomPadding,
        ),
        color = AodsTheme.colors.primary,
        style = AodsTheme.typography.labelMedium,
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
            top = AodsTheme.settingsRow.sectionLabelTopPadding,
            bottom = AodsTheme.settingsRow.sectionLabelBottomPadding,
        ),
        color = AodsTheme.colors.onSurfaceVariant,
        style = AodsTheme.typography.bodyMedium,
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
            .heightIn(min = AodsTheme.settingsRow.minHeight)
            .toggleable(value = isChecked, enabled = isEnabled, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AodsTheme.settingsRow.labelEndPadding)
                .alpha(if (isEnabled) 1f else AodsTheme.opacity.disabled),
        ) {
            Text(
                text = label,
                style = AodsTheme.typography.bodyLarge,
            )

            description?.let { text ->
                Text(
                    text = text,
                    modifier = Modifier.padding(top = AodsTheme.spacing.textGap),
                    color = AodsTheme.colors.onSurfaceVariant,
                    style = AodsTheme.typography.bodySmall,
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
            .heightIn(min = AodsTheme.settingsRow.compactMinHeight)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = null)

        Text(
            text = label,
            modifier = Modifier.padding(start = AodsTheme.settingsRow.radioLabelStartPadding),
            fontFamily = labelFontFamily,
            style = AodsTheme.typography.bodyLarge,
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
            .heightIn(min = AodsTheme.settingsRow.minHeight)
            .onClick(shape = AodsTheme.shapes.small, action = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = AodsTheme.settingsRow.labelEndPadding),
        ) {
            Text(
                text = label,
                style = AodsTheme.typography.bodyLarge,
            )

            description?.let { text ->
                Text(
                    text = text,
                    modifier = Modifier.padding(top = AodsTheme.spacing.textGap),
                    color = AodsTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AodsTheme.typography.bodySmall,
                )
            }
        }

        Text(
            text = value,
            color = AodsTheme.colors.primary,
            style = AodsTheme.typography.bodyLarge,
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

    Column(modifier = modifier.padding(top = AodsTheme.settingsRow.sliderTopPadding)) {
        Text(
            text = label,
            style = AodsTheme.typography.bodyLarge,
        )

        Slider(
            value = ((value - valueRange.first) / step).toFloat(),
            onValueChange = { onValueChange(valueRange.first + it.roundToInt() * step) },
            valueRange = 0f..stepCount.toFloat(),
            steps = if (step == 1) 0 else stepCount - 1,
        )
    }
}
