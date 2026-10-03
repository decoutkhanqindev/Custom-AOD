package com.decoutkhanqindev.custom_aod.utils

interface Tag {
    val tag: String get() = this::class.simpleName ?: ""
}
