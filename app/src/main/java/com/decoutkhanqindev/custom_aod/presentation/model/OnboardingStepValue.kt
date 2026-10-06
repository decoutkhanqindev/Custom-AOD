package com.decoutkhanqindev.custom_aod.presentation.model

import androidx.compose.runtime.Immutable

// Các bước onboarding ở lần đầu mở app, đúng thứ tự; thêm bước thì thêm vào đây, số bước hiện trên màn tự đổi theo.
@Immutable
enum class OnboardingStepValue {
    LANGUAGE,
    PERMISSION,
    ;

    val number: Int
        get() = ordinal + 1
}
