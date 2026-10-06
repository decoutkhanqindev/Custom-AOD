package com.decoutkhanqindev.custom_aod.presentation.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsLottie
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
fun SplashContent() {
    val splash = AodsTheme.splash
    val enterDuration = AodsTheme.motion.durationEnter
    val titleState = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AodsTheme.colors.background),
    ) {
        AodsLottie(
            resId = R.raw.lottie_device_edge_light,
            modifier = Modifier
                .align(Alignment.Center)
                .size(splash.iconSize),
            placeholder = {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )

        AnimatedVisibility(
            visibleState = titleState,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = splash.textOffset)
                .padding(horizontal = splash.contentPadding),
            enter = fadeIn(animationSpec = tween(durationMillis = enterDuration)) +
                slideInVertically(animationSpec = tween(durationMillis = enterDuration)) { height -> height / 2 },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = AodsTheme.colors.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    style = AodsTheme.typography.headlineMedium,
                )

                Text(
                    text = stringResource(R.string.splash_tagline),
                    modifier = Modifier.padding(top = AodsTheme.spacing.stackGap),
                    color = AodsTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = AodsTheme.typography.bodyMedium,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(splash.contentPadding),
            verticalArrangement = Arrangement.spacedBy(AodsTheme.spacing.stackGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.loading),
                color = AodsTheme.colors.onSurfaceVariant,
                style = AodsTheme.typography.bodyMedium,
            )

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AodsTheme.colors.primary,
                trackColor = AodsTheme.colors.surfaceContainer,
            )

            Text(
                text = stringResource(R.string.may_contain_ads),
                color = AodsTheme.colors.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                style = AodsTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun SplashContentPreview() {
    AodsTheme {
        SplashContent()
    }
}
