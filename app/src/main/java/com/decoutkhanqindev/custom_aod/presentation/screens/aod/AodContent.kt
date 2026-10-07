package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.decoutkhanqindev.custom_aod.presentation.model.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodAppearanceUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodExtrasUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.CalendarEventUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.ClockColorValue
import com.decoutkhanqindev.custom_aod.presentation.model.ClockFaceValue
import com.decoutkhanqindev.custom_aod.presentation.model.MediaUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.NotificationContentUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.model.WeatherConditionValue
import com.decoutkhanqindev.custom_aod.presentation.model.WeatherUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsAodTheme
import kotlinx.collections.immutable.persistentListOf

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
        animationSpec = tween(durationMillis = 1500),
        label = "AodShiftX",
    )
    val shiftY = animateDpAsState(
        targetValue = state.shiftYDp.dp,
        animationSpec = tween(durationMillis = 1500),
        label = "AodShiftY",
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

        AodWallpaper(wallpaper = state.wallpaper.takeIf { !state.isDark })

        AodBackground(background = state.background.takeIf { !state.isDark })

        AodEdgeGlow(
            isGlowing = state.isGlowing,
            color = state.glowColorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
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

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun AodContentPreview() {
    AodsAodTheme {
        AodContent(
            state = AodState(
                nowMillis = System.currentTimeMillis(),
                extras = AodExtrasUiModel(memo = "Buy milk"),
                wallpaper = WallpaperValue.AURORA,
                notifications = AodNotificationsUiModel(
                    latest = NotificationContentUiModel(
                        key = "preview",
                        icon = createBitmap(48, 48),
                        title = "Alice",
                        text = "See you at 7 at the usual place?",
                        isHidden = false,
                    ),
                ),
                weather = WeatherUiModel(
                    temperature = 24,
                    condition = WeatherConditionValue.CLOUDY,
                    isDay = true,
                    updatedAtMillis = System.currentTimeMillis(),
                ),
                events = persistentListOf(
                    CalendarEventUiModel(
                        title = "Team meeting",
                        beginMillis = System.currentTimeMillis(),
                        endMillis = System.currentTimeMillis(),
                        isAllDay = false,
                    ),
                ),
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
}

@Preview(widthDp = 720, heightDp = 360)
@Composable
private fun AodContentLandscapePreview() {
    AodsAodTheme {
        AodContent(
            state = AodState(
                nowMillis = System.currentTimeMillis(),
                appearance = AodAppearanceUiModel(face = ClockFaceValue.ANALOG, color = ClockColorValue.MINT),
                battery = BatteryUiModel(percent = 72, isCharging = true),
            ),
            onIntent = {},
        )
    }
}
