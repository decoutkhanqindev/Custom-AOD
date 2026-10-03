package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class PermissionUiModel(
    val permission: PermissionValue,
    val isGranted: Boolean?,
) {
    val status: PermissionStatusValue
        get() = when {
            isGranted == true -> PermissionStatusValue.GRANTED
            isGranted == null -> PermissionStatusValue.UNKNOWN
            permission.isRequired -> PermissionStatusValue.MISSING_REQUIRED
            else -> PermissionStatusValue.MISSING_OPTIONAL
        }
}
