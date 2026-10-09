package com.decoutkhanqindev.custom_aod.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.OnboardingStepValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AodsOnboardingTopBar(
    step: OnboardingStepValue,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    containerColor: Color = AodsColors.Black,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.onboarding_step, step.number, OnboardingStepValue.entries.size),
                modifier = Modifier
                    .background(
                        color = AodsColors.MintAlpha12,
                        shape = CircleShape,
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 4.dp,
                    ),
                color = AodsColors.Mint,
                style = AodsTypography.LabelLarge,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
    )
}

@Composable
fun AodsOnboardingHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            color = AodsColors.GreyED,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = AodsTypography.HeadlineMedium,
        )

        Text(
            text = subtitle,
            modifier = Modifier.padding(top = 8.dp),
            color = AodsColors.Grey9A,
            textAlign = TextAlign.Center,
            style = AodsTypography.BodyLarge,
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = disabledHint != null && !isActionEnabled) {
            Text(
                text = disabledHint.orEmpty(),
                modifier = Modifier.padding(bottom = 8.dp),
                color = AodsColors.Grey9A,
                textAlign = TextAlign.Center,
                style = AodsTypography.BodySmall,
            )
        }

        Button(
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            enabled = isActionEnabled,
        ) {
            Text(text = actionLabel)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsOnboardingTopBarPreview() {
    AodsTheme {
        AodsOnboardingTopBar(
            step = OnboardingStepValue.PERMISSION,
            onNavigateBack = {},
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun AodsOnboardingHeaderPreview() {
    AodsTheme {
        AodsOnboardingHeader(
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
