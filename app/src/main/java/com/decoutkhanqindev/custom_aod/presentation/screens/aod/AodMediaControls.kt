package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.components.onClick
import com.decoutkhanqindev.custom_aod.presentation.model.MediaUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors

@Composable
internal fun AodMediaControls(
    media: MediaUiModel?,
    onIntent: (AodIntent) -> Unit,
) {
    AnimatedContent(
        targetState = media,
        contentKey = { it != null },
        label = "AodMediaControls",
    ) { target ->
        if (target != null) {
            Column(
                modifier = Modifier.padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = target.title,
                    modifier = Modifier.widthIn(max = 260.dp),
                    color = AodsColors.Grey8A,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                target.artist?.let { artist ->
                    Text(
                        text = artist,
                        modifier = Modifier.widthIn(max = 260.dp),
                        color = AodsColors.Grey6E,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AodMediaButton(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.aod_media_previous),
                        isEnabled = target.canSkipToPrevious,
                        onClick = { onIntent(AodIntent.SkipToPreviousTrack) },
                    )

                    AodMediaButton(
                        imageVector = if (target.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (target.isPlaying) R.string.aod_media_pause else R.string.aod_media_play,
                        ),
                        isEnabled = true,
                        onClick = { onIntent(AodIntent.PlayPauseMedia) },
                    )

                    AodMediaButton(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.aod_media_next),
                        isEnabled = target.canSkipToNext,
                        onClick = { onIntent(AodIntent.SkipToNextTrack) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AodMediaButton(
    imageVector: ImageVector,
    contentDescription: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(40.dp)
            .then(
                if (isEnabled) {
                    Modifier.onClick(shape = CircleShape, ripple = false, action = onClick)
                } else {
                    Modifier
                },
            )
            .padding(8.dp),
        tint = if (isEnabled) AodsColors.GreyB4 else AodsColors.Grey5A,
    )
}
