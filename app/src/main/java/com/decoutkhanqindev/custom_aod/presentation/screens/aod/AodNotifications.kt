package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.NotificationContentUiModel
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
internal fun AodNotificationIcons(notifications: AodNotificationsUiModel) {
    val clock = AodsTheme.clock

    AnimatedContent(
        targetState = notifications,
        contentKey = { it.icons.isEmpty() },
        label = "AodNotificationIcons",
    ) { target ->
        if (target.icons.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = clock.notificationIconsTopPadding)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(clock.notificationIconGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                target.icons.forEach { icon ->
                    key(icon.packageName) {
                        Image(
                            bitmap = remember(icon.icon) { icon.icon.asImageBitmap() },
                            contentDescription = null,
                            modifier = Modifier.size(clock.iconSize),
                            colorFilter = ColorFilter.tint(clock.text),
                        )
                    }
                }

                if (target.overflowCount > 0) {
                    Text(
                        text = stringResource(R.string.aod_notifications_overflow, target.overflowCount),
                        color = clock.textMuted,
                        fontSize = clock.captionSize,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AodNotificationContent(content: NotificationContentUiModel?) {
    val clock = AodsTheme.clock

    AnimatedContent(
        targetState = content,
        contentKey = { it?.key },
        label = "AodNotificationContent",
    ) { target ->
        if (target != null) {
            Column(
                modifier = Modifier
                    .padding(top = clock.itemGap)
                    .widthIn(max = clock.textMaxWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = remember(target.icon) { target.icon.asImageBitmap() },
                        contentDescription = null,
                        modifier = Modifier.size(clock.smallIconSize),
                        colorFilter = ColorFilter.tint(clock.text),
                    )

                    Text(
                        text = if (target.isHidden) {
                            stringResource(R.string.aod_notification_content_hidden)
                        } else {
                            target.title
                        },
                        modifier = Modifier.padding(start = clock.iconTextGap),
                        color = clock.text,
                        fontSize = clock.bodySize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (target.text.isNotEmpty()) {
                    Text(
                        text = target.text,
                        modifier = Modifier.padding(top = clock.lineGap),
                        color = clock.textMuted,
                        fontSize = clock.detailSize,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
