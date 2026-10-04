package com.decoutkhanqindev.custom_aod.data.device.media

data class MediaPlayback(
    val title: String,
    val artist: String?,
    val isPlaying: Boolean,
    val canSkipToPrevious: Boolean,
    val canSkipToNext: Boolean,
)
