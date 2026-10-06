package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
fun AodsOnboardingHeader(
    step: OnboardingStepValue,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val onboarding = AodsTheme.onboarding

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = onboarding.headerTopPadding),
    ) {
        Text(
            text = stringResource(R.string.onboarding_step, step.number, OnboardingStepValue.entries.size),
            modifier = Modifier
                .background(
                    color = AodsTheme.colors.primary.copy(alpha = AodsTheme.opacity.tint),
                    shape = AodsTheme.shapes.full,
                )
                .padding(
                    horizontal = onboarding.stepPillHorizontalPadding,
                    vertical = onboarding.stepPillVerticalPadding,
                ),
            color = AodsTheme.colors.primary,
            style = AodsTheme.typography.labelLarge,
        )

        Text(
            text = title,
            modifier = Modifier
                .padding(top = onboarding.titleTopPadding)
                .semantics { heading() },
            color = AodsTheme.colors.onBackground,
            fontWeight = FontWeight.SemiBold,
            style = AodsTheme.typography.headlineMedium,
        )

        Text(
            text = subtitle,
            modifier = Modifier.padding(top = onboarding.subtitleTopPadding),
            color = AodsTheme.colors.onSurfaceVariant,
            style = AodsTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun AodsOnboardingFooter(
    actionLabel: String,
    isActionEnabled: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    disabledHint: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(AodsTheme.spacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = disabledHint != null && !isActionEnabled) {
            Text(
                text = disabledHint.orEmpty(),
                modifier = Modifier.padding(bottom = AodsTheme.onboarding.hintBottomPadding),
                color = AodsTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = AodsTheme.typography.bodySmall,
            )
        }

        Button(
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AodsTheme.onboarding.buttonMinHeight),
            enabled = isActionEnabled,
        ) {
            Text(text = actionLabel)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsOnboardingHeaderPreview() {
    AodsTheme {
        AodsOnboardingHeader(
            step = OnboardingStepValue.LANGUAGE,
            title = "Choose your language",
            subtitle = "You can change it later on the home screen.",
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsOnboardingFooterDisabledPreview() {
    AodsTheme {
        AodsOnboardingFooter(
            actionLabel = "Get started",
            isActionEnabled = false,
            onAction = {},
            disabledHint = "Allow every permission above to continue.",
        )
    }
}
