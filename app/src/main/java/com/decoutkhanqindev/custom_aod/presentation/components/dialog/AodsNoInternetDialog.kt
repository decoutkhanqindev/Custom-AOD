package com.decoutkhanqindev.custom_aod.presentation.components.dialog

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
fun AodsNoInternetDialog(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                onClick = {
                    context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                },
            ) {
                Text(
                    text = stringResource(R.string.open_settings),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
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
                style = MaterialTheme.typography.titleMedium,
            )
        },
        shape = MaterialTheme.shapes.large,
    )
}

@Preview
@Composable
private fun AodsNoInternetDialogPreview() {
    AodsTheme {
        AodsNoInternetDialog()
    }
}
