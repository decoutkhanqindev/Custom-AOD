package com.decoutkhanqindev.custom_aod.presentation.components.aod

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.AodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.NotificationContentUiModel
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey6E
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey8A

@Composable
fun AodNotificationIcons(notifications: AodNotificationsUiModel) {
    AnimatedContent(
        targetState = notifications,
        contentKey = { it.icons.isEmpty() },
        label = "AodNotificationIcons",
    ) { target ->
        if (target.icons.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                target.icons.forEach { icon ->
                    key(icon.packageName) {
                        Image(
                            bitmap = remember(icon.icon) { icon.icon.asImageBitmap() },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            colorFilter = ColorFilter.tint(Grey8A),
                        )
                    }
                }

                if (target.overflowCount > 0) {
                    Text(
                        text = stringResource(
                            R.string.aod_notifications_overflow,
                            target.overflowCount
                        ),
                        color = Grey6E,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun AodNotificationContent(content: NotificationContentUiModel?) {
    AnimatedContent(
        targetState = content,
        contentKey = { it?.key },
        label = "AodNotificationContent",
    ) { target ->
        if (target != null) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .widthIn(max = 280.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = remember(target.icon) { target.icon.asImageBitmap() },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        colorFilter = ColorFilter.tint(Grey8A),
                    )

                    Text(
                        text = if (target.isHidden) {
                            stringResource(R.string.aod_notification_content_hidden)
                        } else {
                            target.title
                        },
                        modifier = Modifier.padding(start = 6.dp),
                        color = Grey8A,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (target.text.isNotEmpty()) {
                    Text(
                        text = target.text,
                        modifier = Modifier.padding(top = 2.dp),
                        color = Grey6E,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
