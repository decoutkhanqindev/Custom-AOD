package com.decoutkhanqindev.custom_aod.presentation.components.aod

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.rememberTimeFormatter
import com.decoutkhanqindev.custom_aod.presentation.components.rememberZonedDateTime
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.CalendarEventUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.WeatherUiModel
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey6E
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey8A
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AodWeather(
    weather: WeatherUiModel?,
    fontFamily: FontFamily,
) {
    AnimatedContent(
        targetState = weather,
        contentKey = { it != null },
        label = "AodWeather",
    ) { target ->
        if (target != null) {
            Row(
                modifier = Modifier.padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = target.icon,
                    contentDescription = stringResource(target.condition.labelRes),
                    modifier = Modifier.size(18.dp),
                    tint = Grey8A,
                )

                Text(
                    text = stringResource(R.string.aod_weather_temperature, target.temperature),
                    modifier = Modifier.padding(start = 4.dp),
                    color = Grey8A,
                    fontSize = 16.sp,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

@Composable
fun AodEvents(events: ImmutableList<CalendarEventUiModel>) {
    val timeFormatter = rememberTimeFormatter()

    AnimatedContent(
        targetState = events,
        contentKey = { it.isEmpty() },
        label = "AodEvents",
    ) { target ->
        if (target.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                target.forEach { event ->
                    key(event.beginMillis, event.title) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Grey6E,
                            )

                            Text(
                                text = if (event.isAllDay) {
                                    stringResource(R.string.aod_event_all_day)
                                } else {
                                    rememberZonedDateTime(event.beginMillis).format(timeFormatter)
                                },
                                modifier = Modifier.padding(start = 6.dp),
                                color = Grey6E,
                                fontSize = 13.sp,
                            )

                            Text(
                                text = event.title,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .widthIn(max = 220.dp),
                                color = Grey8A,
                                fontSize = 13.sp,
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
fun AodMemo(
    memo: String,
    fontFamily: FontFamily,
) {
    if (memo.isNotBlank()) {
        Text(
            text = memo,
            modifier = Modifier
                .padding(top = 12.dp)
                .widthIn(max = 280.dp),
            color = Grey8A,
            fontSize = 14.sp,
            fontFamily = fontFamily,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AodDrawing(
    drawing: Bitmap?,
    color: Color,
) {
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
                    .padding(top = 12.dp)
                    .size(120.dp),
                colorFilter = ColorFilter.tint(color),
            )
        }
    }
}
