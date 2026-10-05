package com.decoutkhanqindev.custom_aod.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
fun SplashContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AodsTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.ic_aod),
            contentDescription = null,
            modifier = Modifier.size(AodsTheme.splash.iconSize),
            tint = AodsTheme.colors.primary,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = AodsTheme.splash.contentPadding,
                    end = AodsTheme.splash.contentPadding,
                    bottom = AodsTheme.splash.contentPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.padding(top = AodsTheme.spacing.sectionPadding),
                color = AodsTheme.colors.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = AodsTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AodsTheme.spacing.stackGap),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.loading),
                    style = AodsTheme.typography.bodyMedium,
                    color = AodsTheme.colors.onSurfaceVariant,
                )

                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                Text(
                    text = stringResource(R.string.may_contain_ads),
                    style = AodsTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = AodsTheme.colors.onSurfaceVariant,
                )
            }
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
