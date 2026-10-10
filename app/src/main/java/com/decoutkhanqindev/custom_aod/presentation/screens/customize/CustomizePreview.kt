package com.decoutkhanqindev.custom_aod.presentation.screens.customize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.CustomizePreviewSamples.Companion.toPreviewAodState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeState

@Composable
fun rememberCustomizePreview(state: CustomizeState): AodState {
    val samples = rememberCustomizePreviewSamples()

    return remember(state.draft, state.nowMillis, state.effects.isGlowPreviewing, samples) {
        state.draft.toPreviewAodState(
            nowMillis = state.nowMillis,
            isGlowing = state.effects.isGlowPreviewing,
            samples = samples,
        )
    }
}

@Composable
private fun rememberCustomizePreviewSamples(): CustomizePreviewSamples {
    val notificationTitle = stringResource(R.string.customize_sample_notification_title)
    val notificationText = stringResource(R.string.customize_sample_notification_text)
    val eventTitle = stringResource(R.string.customize_sample_event_title)
    val mediaTitle = stringResource(R.string.customize_sample_media_title)
    val mediaArtist = stringResource(R.string.customize_sample_media_artist)

    return remember(notificationTitle, notificationText, eventTitle, mediaTitle, mediaArtist) {
        CustomizePreviewSamples(
            notificationTitle = notificationTitle,
            notificationText = notificationText,
            eventTitle = eventTitle,
            mediaTitle = mediaTitle,
            mediaArtist = mediaArtist,
        )
    }
}
