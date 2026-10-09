package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.AodsOnboardingHeader
import com.decoutkhanqindev.custom_aod.presentation.components.AodsPermissionCard
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPermissionSheet(
    isVisible: Boolean,
    permissions: ImmutableList<PermissionUiModel>,
    onIntent: (MainIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShown by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isShown = true
        } else if (isShown) {
            sheetState.hide()
            isShown = false
        }
    }

    if (isShown) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(MainIntent.DismissPermissionSheet) },
            sheetState = sheetState,
            containerColor = AodsColors.Neutral12,
        ) {
            PermissionSheetBody(permissions = permissions, onIntent = onIntent)
        }
    }
}

@Composable
fun MainPermissionWarning(
    isVisible: Boolean,
    onIntent: (MainIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = isVisible, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(AodsShapes.RoundedCornerShape16dp)
                .background(AodsColors.RedAlpha12)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AodsColors.Red,
            )

            Text(
                text = stringResource(R.string.permission_sheet_title),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                color = AodsColors.GreyED,
                style = AodsTypography.BodyLarge,
            )

            TextButton(onClick = { onIntent(MainIntent.ShowPermissionSheet) }) {
                Text(text = stringResource(R.string.action_allow))
            }
        }
    }
}

@Composable
private fun PermissionSheetBody(
    permissions: ImmutableList<PermissionUiModel>,
    onIntent: (MainIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AodsOnboardingHeader(
            title = stringResource(R.string.permission_sheet_title),
            subtitle = stringResource(R.string.permission_sheet_desc),
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )

        permissions.forEach { permission ->
            AodsPermissionCard(
                permission = permission,
                onAllowClick = { onIntent(MainIntent.OpenPermissionSettings(permission.permission)) },
                containerColor = AodsColors.Black,
            )
        }

        TextButton(
            onClick = { onIntent(MainIntent.DismissPermissionSheet) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .heightIn(min = 56.dp),
        ) {
            Text(text = stringResource(R.string.action_later))
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun PermissionSheetBodyPreview() {
    AodsTheme {
        Column(modifier = Modifier.background(AodsColors.Neutral12)) {
            PermissionSheetBody(
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = false),
                    PermissionUiModel(permission = PermissionValue.MIUI_LOCK_SCREEN, isGranted = true),
                ),
                onIntent = {},
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun MainPermissionWarningPreview() {
    AodsTheme {
        MainPermissionWarning(isVisible = true, onIntent = {})
    }
}
