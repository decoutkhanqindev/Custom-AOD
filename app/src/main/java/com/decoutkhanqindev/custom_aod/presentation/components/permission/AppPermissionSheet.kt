package com.decoutkhanqindev.custom_aod.presentation.components.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.onboarding.AppOnboardingHeader
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.permission.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.Neutral12
import com.decoutkhanqindev.custom_aod.presentation.theme.Theme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPermissionSheet(
    isVisible: Boolean,
    permissions: ImmutableList<PermissionUiModel>,
    title: String,
    subtitle: String,
    onAllowClick: (PermissionValue) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShown by remember { mutableStateOf(isVisible) }
    var shownPermissions by remember { mutableStateOf(permissions) }

    LaunchedEffect(isVisible, permissions) {
        if (isVisible) {
            shownPermissions = permissions
            isShown = true
        } else if (isShown) {
            sheetState.hide()
            isShown = false
        }
    }

    if (isShown) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Neutral12,
        ) {
            AppPermissionSheetBody(
                permissions = shownPermissions,
                title = title,
                subtitle = subtitle,
                onAllowClick = onAllowClick,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun AppPermissionSheetBody(
    permissions: ImmutableList<PermissionUiModel>,
    title: String,
    subtitle: String,
    onAllowClick: (PermissionValue) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppOnboardingHeader(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )

        permissions.forEach { permission ->
            AppPermissionCard(
                permission = permission,
                onAllowClick = { onAllowClick(permission.permission) },
                containerColor = Black,
            )
        }

        TextButton(
            onClick = onDismiss,
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
private fun AppPermissionSheetBodyPreview() {
    Theme {
        Column(modifier = Modifier.background(Neutral12)) {
            AppPermissionSheetBody(
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = false),
                    PermissionUiModel(
                        permission = PermissionValue.MIUI_LOCK_SCREEN,
                        isGranted = true
                    ),
                ),
                title = "The clock cannot appear yet",
                subtitle = "Allow these permissions so the clock shows when the screen turns off.",
                onAllowClick = {},
                onDismiss = {},
            )
        }
    }
}
