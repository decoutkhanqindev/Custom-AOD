package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class PermissionUiModel(
    val permission: PermissionValue,
    val isGranted: Boolean?,
)
