package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.onClick
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
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
import java.time.format.DateTimeFormatter

@Composable
fun AodContent(
    state: AodState,
    onIntent: (AodIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnIntent by rememberUpdatedState(onIntent)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { currentOnIntent(AodIntent.DoubleTap) })
            },
        contentAlignment = Alignment.Center,
    ) {
        AodEdgeGlow(
            isGlowing = state.isGlowing,
            color = state.glowColorArgb?.let { Color(it) } ?: Mint,
        )

        AnimatedVisibility(
            visible = !state.isDark,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                modifier = Modifier.offset {
                    IntOffset(
                        x = shiftX.value.roundToPx(),
                        y = shiftY.value.roundToPx()
                    )
                },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AodClock(nowMillis = state.nowMillis)

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

                AodMediaControls(
                    media = state.media,
                    onIntent = currentOnIntent,
                )

                Spacer(modifier = Modifier.height(28.dp))
                AodExitHint(isVisible = state.isHintVisible)
            }
        }
    }
}

@Composable
private fun AodClock(nowMillis: Long) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val timePattern = stringResource(
        if (DateFormat.is24HourFormat(context)) R.string.aod_time_pattern_24h else R.string.aod_time_pattern_12h,
    )
    val datePattern = stringResource(R.string.aod_date_pattern)
    val timeFormatter = remember(timePattern, locale) {
        DateTimeFormatter.ofPattern(timePattern, locale)
    }
    val dateFormatter = remember(datePattern, locale) {
        DateTimeFormatter.ofPattern(datePattern, locale)
    }
    val now = remember(nowMillis) {
        Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = now.format(timeFormatter),
            color = GreyB4,
            fontSize = 76.sp,
            fontWeight = FontWeight.Thin,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = now.format(dateFormatter),
            color = Grey8A,
            fontSize = 16.sp,
        )
    }
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
                        onClick = { onIntent(AodIntent.MediaSkipPrevious) },
                    )

                    AodMediaButton(
                        imageVector = if (target.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (target.isPlaying) R.string.aod_media_pause else R.string.aod_media_play,
                        ),
                        isEnabled = true,
                        onClick = { onIntent(AodIntent.MediaPlayPause) },
                    )

                    AodMediaButton(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.aod_media_next),
                        isEnabled = target.canSkipToNext,
                        onClick = { onIntent(AodIntent.MediaSkipNext) },
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
