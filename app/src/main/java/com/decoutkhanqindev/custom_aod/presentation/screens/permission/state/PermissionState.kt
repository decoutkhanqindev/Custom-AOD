package com.decoutkhanqindev.custom_aod.presentation.screens.permission.state

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.presentation.model.PermissionUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.hasRequiredPermissions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PermissionState(
    val permissions: ImmutableList<PermissionUiModel> = persistentListOf(),
) {
    // Chỉ khoá nút khi chắc chắn còn thiếu quyền; quyền không đọc được trạng thái vẫn cho đi tiếp.
    val isConfirmEnabled: Boolean
        get() = permissions.isNotEmpty() && permissions.hasRequiredPermissions
}
