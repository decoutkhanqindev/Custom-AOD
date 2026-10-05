package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import com.decoutkhanqindev.custom_aod.presentation.model.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme

@Composable
internal fun AodWallpaper(wallpaper: WallpaperValue?) {
    val alpha = AodsTheme.clock.imageAlpha

    Crossfade(
        targetState = wallpaper,
        modifier = Modifier.fillMaxSize(),
        label = "AodWallpaper",
    ) { target ->
        if (target != null) {
            Image(
                painter = painterResource(target.drawableRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = target.alignment,
                contentScale = ContentScale.Crop,
                alpha = alpha,
            )
        }
    }
}

@Composable
internal fun AodBackground(background: Bitmap?) {
    val alpha = AodsTheme.clock.imageAlpha

    Crossfade(
        targetState = background,
        modifier = Modifier.fillMaxSize(),
        label = "AodBackground",
    ) { bitmap ->
        if (bitmap != null) {
            Image(
                bitmap = remember(bitmap) { bitmap.asImageBitmap() },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = alpha,
            )
        }
    }
}

@Composable
internal fun AodEdgeGlow(
    isGlowing: Boolean,
    color: Color,
) {
    val clock = AodsTheme.clock

    AnimatedVisibility(
        visible = isGlowing,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(durationMillis = clock.glowInDurationMillis)),
        exit = fadeOut(animationSpec = tween(durationMillis = clock.glowOutDurationMillis)),
    ) {
        val transition = rememberInfiniteTransition(label = "AodEdgeGlow")
        val pulse = transition.animateFloat(
            initialValue = clock.glowPulseMinAlpha,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = clock.glowPulseDurationMillis),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "AodEdgeGlowPulse",
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowWidth = clock.edgeGlowWidth.toPx()
            val edgeColor = color.copy(alpha = pulse.value)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(edgeColor, Color.Transparent),
                    startY = 0f,
                    endY = glowWidth,
                ),
                size = Size(width = size.width, height = glowWidth),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, edgeColor),
                    startY = size.height - glowWidth,
                    endY = size.height,
                ),
                topLeft = Offset(x = 0f, y = size.height - glowWidth),
                size = Size(width = size.width, height = glowWidth),
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(edgeColor, Color.Transparent),
                    startX = 0f,
                    endX = glowWidth,
                ),
                size = Size(width = glowWidth, height = size.height),
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, edgeColor),
                    startX = size.width - glowWidth,
                    endX = size.width,
                ),
                topLeft = Offset(x = size.width - glowWidth, y = 0f),
                size = Size(width = glowWidth, height = size.height),
            )
        }
    }
}
