package com.decoutkhanqindev.custom_aod.presentation.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsLottie
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography

@Composable
fun SplashContent() {
    val enterDuration = 600
    val titleState = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AodsColors.Black),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .padding(bottom = 128.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AodsLottie(
                resId = R.raw.lottie_device_edge_light,
                modifier = Modifier.size(300.dp),
            )

            AnimatedVisibility(
                visibleState = titleState,
                modifier = Modifier.offset(y = (-32).dp),
                enter = fadeIn(animationSpec = tween(durationMillis = enterDuration)) +
                    expandVertically(
                        animationSpec = tween(durationMillis = enterDuration),
                        expandFrom = Alignment.Top,
                    ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = AodsColors.GreyED,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        style = AodsTypography.HeadlineMedium,
                    )

                    Text(
                        text = stringResource(R.string.splash_tagline),
                        modifier = Modifier.padding(top = 8.dp),
                        color = AodsColors.Grey9A,
                        textAlign = TextAlign.Center,
                        style = AodsTypography.BodyMedium,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.loading),
                color = AodsColors.Grey9A,
                style = AodsTypography.BodyMedium,
            )

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AodsColors.Mint,
                trackColor = AodsColors.Neutral12,
            )

            Text(
                text = stringResource(R.string.may_contain_ads),
                color = AodsColors.Grey9A,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                style = AodsTypography.BodySmall,
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
