package com.decoutkhanqindev.custom_aod.presentation.components.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.theme.RoundedCornerShape16dp
import com.decoutkhanqindev.custom_aod.presentation.theme.Theme
import com.decoutkhanqindev.custom_aod.presentation.theme.TitleMedium

@Composable
fun AppNoInternetDialog(modifier: Modifier = Modifier, onOpenSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(
                    text = stringResource(R.string.open_settings),
                    fontWeight = FontWeight.Bold,
                    style = TitleMedium,
                )
            }
        },
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.no_internet_connection),
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                style = TitleMedium,
            )
        },
        shape = RoundedCornerShape16dp,
    )
}

@Preview
@Composable
private fun AppNoInternetDialogPreview() {
    Theme {
        AppNoInternetDialog {}
    }
}
