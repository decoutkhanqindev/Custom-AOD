package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

internal object AodsShapes {
    val RoundedCornerShape8dp = RoundedCornerShape(8.dp)
    val RoundedCornerShape12dp = RoundedCornerShape(12.dp)
    val RoundedCornerShape16dp = RoundedCornerShape(16.dp)
    val RoundedCornerTopShape16dp = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    // Cầu nối cho component Material 3.
    val Material = Shapes(
        small = RoundedCornerShape8dp,
        medium = RoundedCornerShape12dp,
        large = RoundedCornerShape16dp,
    )
}
