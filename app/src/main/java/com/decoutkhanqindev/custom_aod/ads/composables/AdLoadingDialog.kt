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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.ads.ad_unit.AdUnit
import com.decoutkhanqindev.custom_aod.ads.ad_unit.AdUnitState
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsShapes

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
            shape = AodsShapes.RoundedCornerShape12dp,
            colors = CardDefaults.cardColors(containerColor = AodsColors.NeutralVariant30),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = AodsColors.Mint,
                    strokeWidth = 3.dp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.ad_loading),
                    color = AodsColors.Grey9A,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
