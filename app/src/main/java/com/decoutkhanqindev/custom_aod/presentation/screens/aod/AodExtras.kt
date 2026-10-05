package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.CalendarEventUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.WeatherUiModel
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun AodWeather(
    weather: WeatherUiModel?,
    fontFamily: FontFamily,
) {
    val clock = AodsTheme.clock

    AnimatedContent(
        targetState = weather,
        contentKey = { it != null },
        label = "AodWeather",
    ) { target ->
        if (target != null) {
            Row(
                modifier = Modifier.padding(start = clock.itemGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = target.icon,
                    contentDescription = stringResource(target.condition.labelRes),
                    modifier = Modifier.size(clock.iconSize),
                    tint = clock.text,
                )

                Text(
                    text = stringResource(R.string.aod_weather_temperature, target.temperature),
                    modifier = Modifier.padding(start = clock.inlineGap),
                    color = clock.text,
                    fontSize = clock.dateSize,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

@Composable
internal fun AodEvents(events: ImmutableList<CalendarEventUiModel>) {
    val clock = AodsTheme.clock
    val timeFormatter = rememberTimeFormatter()

    AnimatedContent(
        targetState = events,
        contentKey = { it.isEmpty() },
        label = "AodEvents",
    ) { target ->
        if (target.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = clock.itemGap)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(clock.inlineGap),
            ) {
                target.forEach { event ->
                    key(event.beginMillis, event.title) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(clock.smallIconSize),
                                tint = clock.textMuted,
                            )

                            Text(
                                text = if (event.isAllDay) {
                                    stringResource(R.string.aod_event_all_day)
                                } else {
                                    rememberZonedDateTime(event.beginMillis).format(timeFormatter)
                                },
                                modifier = Modifier.padding(start = clock.iconTextGap),
                                color = clock.textMuted,
                                fontSize = clock.detailSize,
                            )

                            Text(
                                text = event.title,
                                modifier = Modifier
                                    .padding(start = clock.textGap)
                                    .widthIn(max = clock.eventTitleMaxWidth),
                                color = clock.text,
                                fontSize = clock.detailSize,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AodMemo(
    memo: String,
    fontFamily: FontFamily,
) {
    if (memo.isNotBlank()) {
        Text(
            text = memo,
            modifier = Modifier
                .padding(top = AodsTheme.clock.itemGap)
                .widthIn(max = AodsTheme.clock.textMaxWidth),
            color = AodsTheme.clock.text,
            fontSize = AodsTheme.clock.bodySize,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun AodDrawing(
    drawing: Bitmap?,
    color: Color,
) {
    val clock = AodsTheme.clock

    AnimatedContent(
        targetState = drawing,
        contentKey = { it != null },
        label = "AodDrawing",
    ) { bitmap ->
        if (bitmap != null) {
            Image(
                bitmap = remember(bitmap) { bitmap.asImageBitmap() },
                contentDescription = stringResource(R.string.aod_drawing),
                modifier = Modifier
                    .padding(top = clock.itemGap)
                    .size(clock.drawingSize),
                colorFilter = ColorFilter.tint(color),
            )
        }
    }
}
