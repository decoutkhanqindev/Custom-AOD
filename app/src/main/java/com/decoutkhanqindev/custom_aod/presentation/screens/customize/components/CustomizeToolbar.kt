package com.decoutkhanqindev.custom_aod.presentation.screens.customize.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeHistoryState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyED
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import com.decoutkhanqindev.custom_aod.presentation.theme.Neutral12
import com.decoutkhanqindev.custom_aod.presentation.theme.NeutralVariant30

@Composable
fun CustomizeToolbar(
    history: CustomizeHistoryState,
    isApplying: Boolean,
    onIntent: (CustomizeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Neutral12,
        contentColor = GreyED,
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onIntent(CustomizeIntent.History.UndoChange) },
                enabled = history.canUndo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = stringResource(R.string.customize_undo),
                )
            }

            IconButton(
                onClick = { onIntent(CustomizeIntent.History.RedoChange) },
                enabled = history.canRedo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = stringResource(R.string.customize_redo),
                )
            }

            IconButton(
                onClick = { onIntent(CustomizeIntent.History.ShowResetConfirm) },
                enabled = !history.isDefault,
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = stringResource(R.string.customize_reset),
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                color = NeutralVariant30,
            )

            Button(
                onClick = { onIntent(CustomizeIntent.ConfirmCustomization) },
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(min = 40.dp),
                enabled = history.hasChanges && !isApplying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Mint,
                    contentColor = Black,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Text(text = stringResource(R.string.customize_apply))
            }
        }
    }
}
