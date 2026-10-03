package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.decoutkhanqindev.custom_aod.R

@Immutable
enum class PermissionStatusValue(
    val icon: ImageVector,
    @param:StringRes val descriptionRes: Int,
) {
    GRANTED(icon = Icons.Default.Check, descriptionRes = R.string.permission_state_granted),
    UNKNOWN(icon = Icons.Default.QuestionMark, descriptionRes = R.string.permission_state_unknown),
    MISSING_REQUIRED(icon = Icons.Default.Close, descriptionRes = R.string.permission_state_missing),
    MISSING_OPTIONAL(icon = Icons.Default.Remove, descriptionRes = R.string.permission_state_missing),
}
