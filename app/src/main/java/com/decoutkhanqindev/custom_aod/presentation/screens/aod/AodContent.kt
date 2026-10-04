package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import android.graphics.Bitmap
import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.onClick
import com.decoutkhanqindev.custom_aod.presentation.model.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.MediaUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey5A
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey6E
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey8A
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyB4
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AodContent(
    state: AodState,
    onIntent: (AodIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnIntent by rememberUpdatedState(onIntent)
    val focusRequester = remember { FocusRequester() }
    val shiftX = animateDpAsState(
        targetValue = state.shiftXDp.dp,
        animationSpec = tween(durationMillis = 1_500),
        label = "AodShiftX",
    )
    val shiftY = animateDpAsState(
        targetValue = state.shiftYDp.dp,
        animationSpec = tween(durationMillis = 1_500),
        label = "AodShiftY",
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .onPreviewKeyEvent { event ->
                val gesture = when (event.key) {
                    Key.VolumeUp -> AodGestureValue.VOLUME_UP
                    Key.VolumeDown -> AodGestureValue.VOLUME_DOWN
                    else -> null
                }
                if (gesture == null || state.interaction.actionOf(gesture) == AodActionValue.NONE) {
                    false
                } else {
                    if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.repeatCount == 0) {
                        currentOnIntent(AodIntent.PerformGesture(gesture))
                    }
                    true
                }
            }
            .focusRequester(focusRequester)
            .focusable()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { currentOnIntent(AodIntent.PerformGesture(AodGestureValue.DOUBLE_TAP)) },
                )
            }
            .pointerInput(Unit) {
                var dragTotal = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onDragEnd = {
                        val threshold = 80.dp.toPx()
                        when {
                            dragTotal <= -threshold ->
                                currentOnIntent(AodIntent.PerformGesture(AodGestureValue.SWIPE_UP))

                            dragTotal >= threshold ->
                                currentOnIntent(AodIntent.PerformGesture(AodGestureValue.SWIPE_DOWN))
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val isLandscape = maxWidth > maxHeight

        AodBackground(background = state.background.takeIf { !state.isDark })

        AodEdgeGlow(
            isGlowing = state.isGlowing,
            color = state.glowColorArgb?.let { Color(it) } ?: Mint,
        )

        AnimatedVisibility(
            visible = !state.isDark,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = shiftY.value.roundToPx(),
                            y = shiftX.value.roundToPx(),
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AodClockFace(nowMillis = state.nowMillis, appearance = state.appearance)

                    Spacer(modifier = Modifier.width(40.dp))

                    AodDetails(state = state, onIntent = currentOnIntent)
                }
            } else {
                Column(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = shiftX.value.roundToPx(),
                            y = shiftY.value.roundToPx(),
                        )
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AodClockFace(nowMillis = state.nowMillis, appearance = state.appearance)

                    AodDetails(state = state, onIntent = currentOnIntent)
                }
            }
        }
    }
}

@Composable
private fun AodDetails(
    state: AodState,
    onIntent: (AodIntent) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))

        AodDate(nowMillis = state.nowMillis, fontFamily = state.appearance.font.fontFamily)

        AodNotificationIcons(notifications = state.notifications)

        state.battery?.let { battery ->
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(
                    if (battery.isCharging) R.string.aod_battery_charging else R.string.aod_battery,
                    battery.percent,
                ),
                color = Grey6E,
                fontSize = 14.sp,
            )
        }

        AnimatedVisibility(visible = state.isFlashlightOn) {
            Icon(
                imageVector = Icons.Default.FlashlightOn,
                contentDescription = stringResource(R.string.aod_flashlight_on),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(18.dp),
                tint = Mint,
            )
        }

        AodMediaControls(
            media = state.media,
            onIntent = onIntent,
        )

        Spacer(modifier = Modifier.height(28.dp))
        AodExitHint(isVisible = state.isExitHintVisible)
    }
}

@Composable
private fun AodClockFace(
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
    val now = rememberZonedNow(nowMillis)
    val timeFormatter = rememberTimeFormatter()

    Text(
        text = now.format(timeFormatter),
        color = appearance.color.color,
        fontSize = 76.sp * appearance.scale,
        fontWeight = FontWeight.Thin,
        fontFamily = appearance.font.fontFamily,
    )
}

@Composable
private fun AodStackedClock(
    nowMillis: Long,
    appearance: AodAppearanceUiModel,
) {
    val now = rememberZonedNow(nowMillis)
    val hourFormatter = rememberFormatter(
        if (is24HourFormat()) R.string.aod_hour_pattern_24h else R.string.aod_hour_pattern_12h,
    )
    val minuteFormatter = rememberFormatter(R.string.aod_minute_pattern)
    val fontSize = 96.sp * appearance.scale

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
    val now = rememberZonedNow(nowMillis)
    val timeDescription = now.format(rememberTimeFormatter())
    val color = appearance.color.color

    Canvas(
        modifier = Modifier
            .size(200.dp * appearance.scale)
            .semantics { contentDescription = timeDescription },
    ) {
        val radius = size.minDimension / 2
        if (hasTicks) {
            repeat(12) { index ->
                val isMajor = index % 3 == 0
                rotate(degrees = index * 30f) {
                    drawLine(
                        color = if (isMajor) color else Grey6E,
                        start = Offset(x = center.x, y = center.y - radius),
                        end = Offset(x = center.x, y = center.y - radius + (if (isMajor) 16.dp else 8.dp).toPx()),
                        strokeWidth = (if (isMajor) 3.dp else 2.dp).toPx(),
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
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        rotate(degrees = now.minute * 6f) {
            drawLine(
                color = color,
                start = center,
                end = Offset(x = center.x, y = center.y - radius * 0.78f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = color, radius = 5.dp.toPx(), center = center)
    }
}

@Composable
private fun AodDate(
    nowMillis: Long,
    fontFamily: FontFamily,
) {
    val now = rememberZonedNow(nowMillis)
    val dateFormatter = rememberFormatter(R.string.aod_date_pattern)

    Text(
        text = now.format(dateFormatter),
        color = Grey8A,
        fontSize = 16.sp,
        fontFamily = fontFamily,
    )
}

@Composable
private fun AodNotificationIcons(notifications: AodNotificationsUiModel) {
    AnimatedContent(
        targetState = notifications,
        contentKey = { it.icons.isEmpty() },
        label = "AodNotificationIcons",
    ) { target ->
        if (target.icons.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                target.icons.forEach { icon ->
                    key(icon.packageName) {
                        Image(
                            bitmap = remember(icon.icon) { icon.icon.asImageBitmap() },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(Grey8A),
                        )
                    }
                }

                if (target.overflowCount > 0) {
                    Text(
                        text = stringResource(R.string.aod_notifications_overflow, target.overflowCount),
                        color = Grey6E,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AodMediaControls(
    media: MediaUiModel?,
    onIntent: (AodIntent) -> Unit,
) {
    AnimatedContent(
        targetState = media,
        contentKey = { it != null },
        label = "AodMediaControls",
    ) { target ->
        if (target != null) {
            Column(
                modifier = Modifier.padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = target.title,
                    modifier = Modifier.widthIn(max = 260.dp),
                    color = Grey8A,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                target.artist?.let { artist ->
                    Text(
                        text = artist,
                        modifier = Modifier.widthIn(max = 260.dp),
                        color = Grey6E,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AodMediaButton(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.aod_media_previous),
                        isEnabled = target.canSkipToPrevious,
                        onClick = { onIntent(AodIntent.SkipToPreviousTrack) },
                    )

                    AodMediaButton(
                        imageVector = if (target.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (target.isPlaying) R.string.aod_media_pause else R.string.aod_media_play,
                        ),
                        isEnabled = true,
                        onClick = { onIntent(AodIntent.PlayPauseMedia) },
                    )

                    AodMediaButton(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.aod_media_next),
                        isEnabled = target.canSkipToNext,
                        onClick = { onIntent(AodIntent.SkipToNextTrack) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AodMediaButton(
    imageVector: ImageVector,
    contentDescription: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(40.dp)
            .then(if (isEnabled) Modifier.onClick(shape = CircleShape, ripple = false, action = onClick) else Modifier)
            .padding(8.dp),
        tint = if (isEnabled) GreyB4 else Grey5A,
    )
}

@Composable
private fun AodBackground(background: Bitmap?) {
    Crossfade(
        targetState = background,
        modifier = Modifier.fillMaxSize(),
        label = "AodBackground",
    ) { bitmap ->
        if (bitmap != null) {
            Image(
                bitmap = remember(bitmap) { bitmap.asImageBitmap() },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f,
            )
        }
    }
}

@Composable
private fun AodEdgeGlow(
    isGlowing: Boolean,
    color: Color,
) {
    AnimatedVisibility(
        visible = isGlowing,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = fadeOut(animationSpec = tween(durationMillis = 800)),
    ) {
        val transition = rememberInfiniteTransition(label = "AodEdgeGlow")
        val pulse = transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "AodEdgeGlowPulse",
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowWidth = 24.dp.toPx()
            val edgeColor = color.copy(alpha = pulse.value)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(edgeColor, Color.Transparent),
                    startY = 0f,
                    endY = glowWidth,
                ),
                size = Size(width = size.width, height = glowWidth),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, edgeColor),
                    startY = size.height - glowWidth,
                    endY = size.height,
                ),
                topLeft = Offset(x = 0f, y = size.height - glowWidth),
                size = Size(width = size.width, height = glowWidth),
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(edgeColor, Color.Transparent),
                    startX = 0f,
                    endX = glowWidth,
                ),
                size = Size(width = glowWidth, height = size.height),
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, edgeColor),
                    startX = size.width - glowWidth,
                    endX = size.width,
                ),
                topLeft = Offset(x = size.width - glowWidth, y = 0f),
                size = Size(width = glowWidth, height = size.height),
            )
        }
    }
}

@Composable
private fun AodExitHint(isVisible: Boolean) {
    val alpha = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "AodExitHint",
    )

    Text(
        text = stringResource(R.string.aod_hint),
        modifier = Modifier.alpha(alpha.value),
        color = Grey5A,
        fontSize = 12.sp,
    )
}

@Composable
private fun is24HourFormat(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

@Composable
private fun rememberTimeFormatter(): DateTimeFormatter = rememberFormatter(
    if (is24HourFormat()) R.string.aod_time_pattern_24h else R.string.aod_time_pattern_12h,
)

@Composable
private fun rememberFormatter(@StringRes patternRes: Int): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = stringResource(patternRes)
    return remember(pattern, locale) { DateTimeFormatter.ofPattern(pattern, locale) }
}

@Composable
private fun rememberZonedNow(nowMillis: Long): ZonedDateTime = remember(nowMillis) {
    Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun AodContentPreview() {
    AodContent(
        state = AodState(
            nowMillis = System.currentTimeMillis(),
            battery = BatteryUiModel(percent = 72, isCharging = false),
            media = MediaUiModel(
                title = "Song title",
                artist = "Artist",
                isPlaying = true,
                canSkipToPrevious = true,
                canSkipToNext = true,
            ),
            isGlowing = true,
        ),
        onIntent = {},
    )
}

@Preview(widthDp = 720, heightDp = 360)
@Composable
private fun AodContentLandscapePreview() {
    AodContent(
        state = AodState(
            nowMillis = System.currentTimeMillis(),
            appearance = AodAppearanceUiModel(face = ClockFaceValue.ANALOG, color = ClockColorValue.MINT),
            battery = BatteryUiModel(percent = 72, isCharging = true),
        ),
        onIntent = {},
    )
}
