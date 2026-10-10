package com.decoutkhanqindev.custom_aod.presentation.model.aod.info

import androidx.compose.runtime.Immutable
import com.decoutkhanqindev.custom_aod.data.device.media.MediaPlayback

@Immutable
data class MediaUiModel(
    val title: String,
    val artist: String?,
    val isPlaying: Boolean,
    val canSkipToPrevious: Boolean,
    val canSkipToNext: Boolean,
)

fun MediaPlayback.toUiModel(): MediaUiModel = MediaUiModel(
    title = title,
    artist = artist,
    isPlaying = isPlaying,
    canSkipToPrevious = canSkipToPrevious,
    canSkipToNext = canSkipToNext,
)
