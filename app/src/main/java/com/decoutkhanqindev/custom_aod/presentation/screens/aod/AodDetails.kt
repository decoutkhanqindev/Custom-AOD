package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors

@Composable
internal fun AodDetails(
    state: AodState,
    onIntent: (AodIntent) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AodDate(nowMillis = state.nowMillis, fontFamily = state.appearance.font.fontFamily)

            AodWeather(weather = state.weather, fontFamily = state.appearance.font.fontFamily)
        }

        AodEvents(events = state.events)

        AodMemo(memo = state.extras.memo, fontFamily = state.appearance.font.fontFamily)

        AodDrawing(drawing = state.drawing, color = state.appearance.color.color)

        AodNotificationIcons(notifications = state.notifications)

        AodNotificationContent(content = state.notifications.latest)

        state.battery?.let { battery ->
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(
                    if (battery.isCharging) R.string.aod_battery_charging else R.string.aod_battery,
                    battery.percent,
                ),
                color = AodsColors.Grey6E,
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
                tint = AodsColors.Mint,
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
private fun AodDate(
    nowMillis: Long,
    fontFamily: FontFamily,
) {
    val now = rememberZonedDateTime(nowMillis)
    val dateFormatter = rememberFormatter(R.string.aod_date_pattern)

    Text(
        text = now.format(dateFormatter),
        color = AodsColors.Grey8A,
        fontSize = 16.sp,
        fontFamily = fontFamily,
    )
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
        color = AodsColors.Grey5A,
        fontSize = 12.sp,
    )
}
