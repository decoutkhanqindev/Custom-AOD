package com.decoutkhanqindev.custom_aod.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// UI đọc qua MaterialTheme.shapes: small 8dp, medium 12dp, large 16dp; hình tròn dùng thẳng CircleShape.
internal object AodsShapes {
    val Material = Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
    )
}
