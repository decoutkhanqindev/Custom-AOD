package com.decoutkhanqindev.custom_aod.ads.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.ads.ad_unit.AdUnit
import com.decoutkhanqindev.custom_aod.ads.ad_unit.AdUnitState
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
fun AdLoadingDialog(adUnit: () -> AdUnit) {
    val adUnitState by adUnit().state.collectAsStateWithLifecycle()

    if (adUnitState != AdUnitState.LOADING) return

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
    ) {
        Card(
            shape = AodsTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = AodsTheme.colors.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = AodsTheme.elevation.low),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AodsTheme.ad.loadingPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AodsTheme.ad.loadingIndicatorSize),
                    color = AodsTheme.colors.primary,
                    strokeWidth = AodsTheme.ad.loadingIndicatorStrokeWidth,
                )

                Spacer(modifier = Modifier.height(AodsTheme.ad.loadingGap))

                Text(
                    text = stringResource(R.string.ad_loading),
                    color = AodsTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
