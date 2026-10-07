package com.decoutkhanqindev.custom_aod.utils

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import timber.log.Timber

private const val NAV_TAG = "Navigation"

fun NavBackStack<NavKey>.navigateTo(destination: NavKey, preserveState: Boolean = true) {
    Timber.tag(NAV_TAG)
        .d("navigateTo ${destination::class.simpleName}, preserveState=$preserveState, from=${map { it::class.simpleName }}")
    val existingIndex = indexOfFirst { it::class == destination::class }

    if (preserveState) {
        if (existingIndex >= 0) {
            while (size > existingIndex + 1) removeLastOrNull()
            if (last() != destination) {
                removeLastOrNull()
                add(destination)
            }
        } else {
            val root = first()
            clear()
            add(root)
            if (destination::class != root::class) add(destination)
        }
    } else {
        clear()
        add(destination)
    }
}

// NavDisplay crash khi back stack rỗng, nên không bao giờ pop màn cuối cùng (bấm back liên tiếp cũng không sao).
fun NavBackStack<NavKey>.navigateBack() {
    Timber.tag(NAV_TAG).d("navigateBack, from=${map { it::class.simpleName }}")
    if (size > 1) removeLastOrNull()
}
