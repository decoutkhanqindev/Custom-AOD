package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
internal fun AodClockFace(
    nowMillis: Long,
    appearance: AodAppearanceUiModel,
) {
    when (appearance.face) {
        ClockFaceValue.DIGITAL -> AodDigitalClock(nowMillis = nowMillis, appearance = appearance)
        ClockFaceValue.STACKED -> AodStackedClock(nowMillis = nowMillis, appearance = appearance)
        ClockFaceValue.ANALOG -> AodAnalogClock(nowMillis = nowMillis, appearance = appearance, hasTicks = true)
        ClockFaceValue.ANALOG_MINIMAL -> AodAnalogClock(nowMillis = nowMillis, appearance = appearance, hasTicks = false)
    }
}

@Composable
private fun AodDigitalClock(
    nowMillis: Long,
    appearance: AodAppearanceUiModel,
) {
    val now = rememberZonedDateTime(nowMillis)
    val timeFormatter = rememberTimeFormatter()

    Text(
        text = now.format(timeFormatter),
        color = appearance.color.color,
        fontSize = AodsTheme.clock.digitalTimeSize * appearance.scale,
        fontWeight = FontWeight.Thin,
        fontFamily = appearance.font.fontFamily,
    )
}

@Composable
private fun AodStackedClock(
    nowMillis: Long,
    appearance: AodAppearanceUiModel,
) {
    val now = rememberZonedDateTime(nowMillis)
    val hourFormatter = rememberFormatter(
        if (is24HourFormat()) R.string.aod_hour_pattern_24h else R.string.aod_hour_pattern_12h,
    )
    val minuteFormatter = rememberFormatter(R.string.aod_minute_pattern)
    val fontSize = AodsTheme.clock.stackedTimeSize * appearance.scale

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = now.format(hourFormatter),
            color = appearance.color.color,
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            fontFamily = appearance.font.fontFamily,
            lineHeight = fontSize,
        )

        Text(
            text = now.format(minuteFormatter),
            color = appearance.color.color,
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            fontFamily = appearance.font.fontFamily,
            lineHeight = fontSize,
        )
    }
}

@Composable
private fun AodAnalogClock(
    nowMillis: Long,
    appearance: AodAppearanceUiModel,
    hasTicks: Boolean,
) {
    val clock = AodsTheme.clock
    val now = rememberZonedDateTime(nowMillis)
    val timeDescription = now.format(rememberTimeFormatter())
    val color = appearance.color.color

    Canvas(
        modifier = Modifier
            .size(clock.analogSize * appearance.scale)
            .semantics { contentDescription = timeDescription },
    ) {
        val radius = size.minDimension / 2
        if (hasTicks) {
            repeat(12) { index ->
                val isMajor = index % 3 == 0
                rotate(degrees = index * 30f) {
                    drawLine(
                        color = if (isMajor) color else clock.textMuted,
                        start = Offset(x = center.x, y = center.y - radius),
                        end = Offset(
                            x = center.x,
                            y = center.y - radius +
                                (if (isMajor) clock.analogMajorTickLength else clock.analogMinorTickLength).toPx(),
                        ),
                        strokeWidth = (if (isMajor) clock.analogMajorTickWidth else clock.analogMinorTickWidth).toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
        rotate(degrees = now.hour % 12 * 30f + now.minute * 0.5f) {
            drawLine(
                color = color,
                start = center,
                end = Offset(x = center.x, y = center.y - radius * 0.5f),
                strokeWidth = clock.analogHourHandWidth.toPx(),
                cap = StrokeCap.Round,
            )
        }
        rotate(degrees = now.minute * 6f) {
            drawLine(
                color = color,
                start = center,
                end = Offset(x = center.x, y = center.y - radius * 0.78f),
                strokeWidth = clock.analogMinuteHandWidth.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = color, radius = clock.analogCenterRadius.toPx(), center = center)
    }
}
