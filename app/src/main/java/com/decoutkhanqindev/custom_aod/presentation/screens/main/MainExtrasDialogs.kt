package com.decoutkhanqindev.custom_aod.presentation.screens.main

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.decoutkhanqindev.custom_aod.R
import com.decoutkhanqindev.custom_aod.presentation.model.AodExtrasUiModel
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsColors
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsShapes
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTypography

@Composable
internal fun MemoEditorDialog(
    memo: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val memoState = rememberTextFieldState(initialText = memo)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(memoState.text.toString()) }) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
        title = { Text(text = stringResource(R.string.opt_memo)) },
        text = {
            OutlinedTextField(
                state = memoState,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.memo_hint)) },
                supportingText = {
                    Text(
                        text = stringResource(
                            R.string.memo_length,
                            memoState.text.length,
                            AodExtrasUiModel.MEMO_MAX_LENGTH,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                inputTransformation = InputTransformation.maxLength(AodExtrasUiModel.MEMO_MAX_LENGTH),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 3),
            )
        },
    )
}

@Composable
internal fun DrawingPadDialog(
    onConfirm: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 8.dp.toPx() }
    val strokes = remember { mutableStateListOf<SnapshotStateList<Offset>>() }
    var padSize by remember { mutableStateOf(IntSize.Zero) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        renderDrawing(
                            strokes = strokes,
                            size = padSize,
                            density = density,
                            strokeWidthPx = strokeWidthPx,
                            strokeColor = AodsColors.White,
                        ),
                    )
                },
                enabled = strokes.isNotEmpty(),
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { strokes.clear() },
                    enabled = strokes.isNotEmpty(),
                ) {
                    Text(text = stringResource(R.string.action_clear))
                }

                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        },
        title = { Text(text = stringResource(R.string.opt_drawing)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(AodsShapes.RoundedCornerShape12dp)
                    .background(AodsColors.Black)
                    .border(width = 1.dp, color = AodsColors.NeutralVariant60, shape = AodsShapes.RoundedCornerShape12dp)
                    .onSizeChanged { size -> padSize = size }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val stroke = mutableStateListOf(down.position)
                            strokes.add(stroke)
                            drag(down.id) { change ->
                                change.consume()
                                stroke.add(change.position)
                            }
                        }
                    }
                    .drawBehind {
                        strokes.forEach { stroke -> drawStroke(points = stroke, color = AodsColors.White, widthPx = strokeWidthPx) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (strokes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.drawing_hint),
                        color = AodsColors.Grey6E,
                        style = AodsTypography.BodyMedium,
                    )
                }
            }
        },
    )
}

private fun DrawScope.drawStroke(
    points: List<Offset>,
    color: Color,
    widthPx: Float,
) {
    if (points.size == 1) {
        drawCircle(color = color, radius = widthPx / 2, center = points.first())
    } else {
        drawPoints(
            points = points,
            pointMode = PointMode.Polygon,
            color = color,
            strokeWidth = widthPx,
            cap = StrokeCap.Round,
        )
    }
}

private fun renderDrawing(
    strokes: List<List<Offset>>,
    size: IntSize,
    density: Density,
    strokeWidthPx: Float,
    strokeColor: Color,
): Bitmap {
    val image = ImageBitmap(width = size.width, height = size.height)
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(image),
        size = size.toSize(),
    ) {
        strokes.forEach { stroke -> drawStroke(points = stroke, color = strokeColor, widthPx = strokeWidthPx) }
    }
    return image.asAndroidBitmap()
}
