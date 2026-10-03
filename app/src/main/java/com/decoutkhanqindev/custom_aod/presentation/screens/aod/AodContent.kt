package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey5A
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey6E
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey8A
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyB4
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AodContent(
    state: AodState,
    onIntent: (AodIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnIntent by rememberUpdatedState(onIntent)
    val shiftX by animateDpAsState(
        targetValue = state.shiftXDp.dp,
        animationSpec = tween(durationMillis = SHIFT_ANIMATION_MILLIS),
        label = "AodShiftX",
    )
    val shiftY by animateDpAsState(
        targetValue = state.shiftYDp.dp,
        animationSpec = tween(durationMillis = SHIFT_ANIMATION_MILLIS),
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
        AnimatedVisibility(
            visible = !state.isDark,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                modifier = Modifier.offset { IntOffset(x = shiftX.roundToPx(), y = shiftY.roundToPx()) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AodClock()

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

                Spacer(modifier = Modifier.height(28.dp))
                AodExitHint()
            }
        }
    }
}

@Composable
private fun AodClock() {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val now by rememberNow()
    val timeFormatter = remember(context, locale) {
        DateTimeFormatter.ofPattern(
            if (DateFormat.is24HourFormat(context)) TIME_PATTERN_24H else TIME_PATTERN_12H,
            locale,
        )
    }
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern(DATE_PATTERN, locale) }

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
private fun rememberNow(): State<LocalDateTime> = produceState(initialValue = LocalDateTime.now()) {
    while (true) {
        delay(MINUTE_MILLIS - System.currentTimeMillis() % MINUTE_MILLIS + TICK_SLACK_MILLIS)
        value = LocalDateTime.now()
    }
}

@Composable
private fun AodExitHint() {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(HINT_VISIBLE_MILLIS)
        isVisible = false
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = HINT_FADE_MILLIS),
        label = "AodExitHint",
    )

    Text(
        text = stringResource(R.string.aod_hint),
        modifier = Modifier.alpha(alpha),
        color = Grey5A,
        fontSize = 12.sp,
    )
}

private const val TIME_PATTERN_24H = "HH:mm"
private const val TIME_PATTERN_12H = "h:mm"
private const val DATE_PATTERN = "EEE, d MMM"
private const val MINUTE_MILLIS = 60_000L
private const val TICK_SLACK_MILLIS = 50L
private const val HINT_VISIBLE_MILLIS = 3_000L
private const val HINT_FADE_MILLIS = 600
private const val SHIFT_ANIMATION_MILLIS = 1_500

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun AodContentPreview() {
    AodContent(
        state = AodState(battery = BatteryUiModel(percent = 72, isCharging = false)),
        onIntent = {},
    )
}
