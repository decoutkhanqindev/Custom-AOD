package com.decoutkhanqindev.custom_aod.presentation.screens.main

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.AnimationContentKey
import com.decoutkhanqindev.custom_aod.presentation.model.AodOptionsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionValue
import com.decoutkhanqindev.custom_aod.presentation.model.WakeResultValue
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.main.state.MainState
import com.decoutkhanqindev.custom_aod.presentation.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt

@Composable
fun MainContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AnimatedContent(
            targetState = if (state.isLoading) AnimationContentKey.Loading else AnimationContentKey.Content,
            label = "MainContent",
        ) { contentKey ->
            when (contentKey) {
                AnimationContentKey.Content -> MainSettings(
                    state = state,
                    onIntent = onIntent,
                    contentPadding = innerPadding,
                )

                AnimationContentKey.Loading,
                AnimationContentKey.Error -> Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun MainSettings(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = stringResource(R.string.main_subtitle),
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SwitchRow(
            labelRes = R.string.opt_enabled,
            isChecked = state.options.isEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleAod(it)) },
        )

        SectionHeader(titleRes = R.string.section_permissions)

        state.permissions.forEach { permission ->
            PermissionRow(
                permission = permission,
                onOpen = { onIntent(MainIntent.OpenPermission(permission.permission)) },
            )
        }

        SectionHeader(titleRes = R.string.section_options)

        SwitchRow(
            labelRes = R.string.opt_dim,
            isChecked = state.options.isDimBrightness,
            onCheckedChange = { onIntent(MainIntent.ToggleDimBrightness(it)) },
        )

        SwitchRow(
            labelRes = R.string.opt_proximity,
            isChecked = state.options.isProximityEnabled,
            onCheckedChange = { onIntent(MainIntent.ToggleProximity(it)) },
        )

        StepSlider(
            label = if (state.options.timeoutMinutes == 0) {
                stringResource(R.string.opt_timeout_never)
            } else {
                stringResource(R.string.opt_timeout_value, state.options.timeoutMinutes)
            },
            value = state.options.timeoutMinutes,
            step = TIMEOUT_STEP_MINUTES,
            max = TIMEOUT_MAX_MINUTES,
            onValueChange = { onIntent(MainIntent.ChangeTimeout(it)) },
        )

        StepSlider(
            label = if (state.options.minBattery == 0) {
                stringResource(R.string.opt_battery_off)
            } else {
                stringResource(R.string.opt_battery_value, state.options.minBattery)
            },
            value = state.options.minBattery,
            step = BATTERY_STEP_PERCENT,
            max = BATTERY_MAX_PERCENT,
            onValueChange = { onIntent(MainIntent.ChangeMinBattery(it)) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onIntent(MainIntent.Preview) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.action_preview))
        }

        Text(
            text = stringResource(state.lastWakeMessageRes),
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SectionHeader(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes).uppercase(),
        modifier = Modifier.padding(top = 28.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun SwitchRow(
    @StringRes labelRes: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = isChecked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )

        Switch(checked = isChecked, onCheckedChange = null)
    }
}

@Composable
private fun PermissionRow(
    permission: PermissionUiModel,
    onOpen: () -> Unit,
) {
    val status = when {
        permission.isGranted == true -> PermissionStatus(
            icon = Icons.Default.Check,
            color = MaterialTheme.colorScheme.primary,
            descriptionRes = R.string.permission_state_granted,
        )

        permission.isGranted == null -> PermissionStatus(
            icon = Icons.Default.QuestionMark,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            descriptionRes = R.string.permission_state_unknown,
        )

        permission.permission.isRequired -> PermissionStatus(
            icon = Icons.Default.Close,
            color = MaterialTheme.colorScheme.error,
            descriptionRes = R.string.permission_state_missing,
        )

        else -> PermissionStatus(
            icon = Icons.Default.Remove,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            descriptionRes = R.string.permission_state_missing,
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(28.dp)) {
            Icon(
                imageVector = status.icon,
                contentDescription = stringResource(status.descriptionRes),
                modifier = Modifier.size(20.dp),
                tint = status.color,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        ) {
            Text(
                text = stringResource(permission.permission.titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = stringResource(permission.permission.descriptionRes),
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        OutlinedButton(onClick = onOpen) {
            Text(text = stringResource(R.string.action_open))
        }
    }
}

@Composable
private fun StepSlider(
    label: String,
    value: Int,
    step: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )

        Slider(
            value = (value / step).toFloat(),
            onValueChange = { onValueChange(it.roundToInt() * step) },
            valueRange = 0f..(max / step).toFloat(),
            steps = max / step - 1,
        )
    }
}

private class PermissionStatus(
    val icon: ImageVector,
    val color: Color,
    @param:StringRes val descriptionRes: Int,
)

private const val TIMEOUT_STEP_MINUTES = 5
private const val TIMEOUT_MAX_MINUTES = 120
private const val BATTERY_STEP_PERCENT = 5
private const val BATTERY_MAX_PERCENT = 50

@Preview(widthDp = 360, heightDp = 900)
@Composable
private fun MainContentPreview() {
    AppTheme {
        MainContent(
            state = MainState(
                isLoading = false,
                options = AodOptionsUiModel(timeoutMinutes = 30),
                permissions = persistentListOf(
                    PermissionUiModel(permission = PermissionValue.OVERLAY, isGranted = true),
                    PermissionUiModel(permission = PermissionValue.MIUI_LOCK_SCREEN, isGranted = false),
                    PermissionUiModel(permission = PermissionValue.MIUI_BACKGROUND_POPUP, isGranted = null),
                    PermissionUiModel(permission = PermissionValue.NOTIFICATIONS, isGranted = false),
                ),
                lastWakeMessageRes = WakeResultValue.OK.messageRes,
            ),
            onIntent = {},
        )
    }
}
