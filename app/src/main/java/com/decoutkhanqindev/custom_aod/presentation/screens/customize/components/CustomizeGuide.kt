package com.decoutkhanqindev.custom_aod.presentation.screens.customize.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeGuideStepValue
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.Black
import com.decoutkhanqindev.custom_aod.presentation.theme.BlackAlpha70
import com.decoutkhanqindev.custom_aod.presentation.theme.BodyMedium
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey9A
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyED
import com.decoutkhanqindev.custom_aod.presentation.theme.LabelMedium
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import com.decoutkhanqindev.custom_aod.presentation.theme.Neutral12
import com.decoutkhanqindev.custom_aod.presentation.theme.NeutralVariant30
import com.decoutkhanqindev.custom_aod.presentation.theme.RoundedCornerShape16dp
import com.decoutkhanqindev.custom_aod.presentation.theme.TitleMedium

@Composable
fun CustomizeGuideOverlay(
    step: CustomizeGuideStepValue,
    targets: Map<CustomizeGuideStepValue, Rect>,
    onIntent: (CustomizeIntent) -> Unit,
) {
    val origin = remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                if (origin.value != position) origin.value = position
            }
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
        ) {
            drawRect(color = BlackAlpha70)
            val spotlight =
                targets[step]?.translate(-origin.value)?.inflate(4.dp.toPx()) ?: return@Canvas
            val cornerRadius = CornerRadius(16.dp.toPx())
            drawRoundRect(
                color = Black,
                topLeft = spotlight.topLeft,
                size = spotlight.size,
                cornerRadius = cornerRadius,
                blendMode = BlendMode.Clear,
            )
            drawRoundRect(
                color = Mint,
                topLeft = spotlight.topLeft,
                size = spotlight.size,
                cornerRadius = cornerRadius,
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        CustomizeGuideBubble(
            step = step,
            onIntent = onIntent,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minHeight = 0))
                    val spotlight = targets[step]?.translate(-origin.value)?.inflate(4.dp.toPx())
                    val gap = 16.dp.toPx()
                    val y = when {
                        spotlight == null -> (constraints.maxHeight - placeable.height) / 2f
                        spotlight.center.y > constraints.maxHeight / 2 -> spotlight.top - gap - placeable.height
                        else -> spotlight.bottom + gap
                    }.toInt()
                        .coerceIn(0, (constraints.maxHeight - placeable.height).coerceAtLeast(0))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, y) }
                },
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlight =
                targets[step]?.translate(-origin.value)?.inflate(4.dp.toPx()) ?: return@Canvas
            val pointerHalf = 8.dp.toPx()
            val borderWidth = 1.dp.toPx()
            val isBubbleAbove = spotlight.center.y > size.height / 2
            val edgeY =
                if (isBubbleAbove) spotlight.top - 16.dp.toPx() else spotlight.bottom + 16.dp.toPx()
            val direction = if (isBubbleAbove) 1f else -1f
            val tip = Offset(spotlight.center.x, edgeY + direction * pointerHalf)
            val left = Offset(spotlight.center.x - pointerHalf, edgeY)
            val right = Offset(spotlight.center.x + pointerHalf, edgeY)
            drawPath(
                path = Path().apply {
                    moveTo(left.x, left.y - direction * borderWidth)
                    lineTo(right.x, right.y - direction * borderWidth)
                    lineTo(tip.x, tip.y)
                    close()
                },
                color = Neutral12,
            )
            drawPath(
                path = Path().apply {
                    moveTo(left.x, left.y)
                    lineTo(tip.x, tip.y)
                    lineTo(right.x, right.y)
                },
                color = NeutralVariant30,
                style = Stroke(width = borderWidth),
            )
        }
    }
}

@Composable
private fun CustomizeGuideBubble(
    step: CustomizeGuideStepValue,
    onIntent: (CustomizeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape16dp,
            color = Neutral12,
            contentColor = GreyED,
            border = BorderStroke(width = 1.dp, color = NeutralVariant30),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AnimatedContent(
                    targetState = step,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "CustomizeGuideText",
                ) { shownStep ->
                    Column {
                        Text(
                            text = stringResource(
                                R.string.customize_guide_progress,
                                shownStep.number,
                                CustomizeGuideStepValue.entries.size,
                            ),
                            color = Mint,
                            style = LabelMedium,
                        )

                        Text(
                            text = stringResource(shownStep.titleRes),
                            modifier = Modifier.padding(top = 4.dp),
                            style = TitleMedium,
                        )

                        Text(
                            text = stringResource(shownStep.descriptionRes),
                            modifier = Modifier.padding(top = 4.dp),
                            color = Grey9A,
                            style = BodyMedium,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { onIntent(CustomizeIntent.Guide.DismissGuide) }) {
                        Text(text = stringResource(R.string.customize_guide_skip))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    CustomizeGuideDots(step = step)

                    Spacer(modifier = Modifier.weight(1f))

                    Button(onClick = { onIntent(CustomizeIntent.Guide.ShowNextGuideStep) }) {
                        Text(
                            text = stringResource(
                                if (step.next == null) R.string.customize_guide_done
                                else R.string.customize_guide_next,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizeGuideDots(step: CustomizeGuideStepValue) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        CustomizeGuideStepValue.entries.forEach { dotStep ->
            val isCurrent = dotStep == step
            val width by animateDpAsState(
                targetValue = if (isCurrent) 16.dp else 6.dp,
                label = "CustomizeGuideDot"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .background(
                        color = if (isCurrent) Mint else NeutralVariant30,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
