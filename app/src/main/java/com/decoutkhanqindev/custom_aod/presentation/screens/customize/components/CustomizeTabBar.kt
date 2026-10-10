package com.decoutkhanqindev.custom_aod.presentation.screens.customize.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.decoutkhanqindev.custom_aod.presentation.components.onClick
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeClusterValue
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeGuideStepValue
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.Grey9A
import com.decoutkhanqindev.custom_aod.presentation.theme.LabelMedium
import com.decoutkhanqindev.custom_aod.presentation.theme.Mint
import com.decoutkhanqindev.custom_aod.presentation.theme.MintAlpha12
import com.decoutkhanqindev.custom_aod.presentation.theme.Neutral12
import com.decoutkhanqindev.custom_aod.presentation.theme.NeutralVariant30
import com.decoutkhanqindev.custom_aod.presentation.theme.RoundedCornerShape12dp
import kotlin.math.roundToInt

@Composable
fun CustomizeTabBar(
    selectedTab: CustomizeTabValue?,
    guideStep: CustomizeGuideStepValue?,
    onIntent: (CustomizeIntent) -> Unit,
    onClusterPositioned: (CustomizeGuideStepValue, Rect) -> Unit,
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val clusterStarts = remember { mutableMapOf<CustomizeClusterValue, Float>() }
    val viewportStart = remember { floatArrayOf(0f) }

    LaunchedEffect(guideStep) {
        if (guideStep == CustomizeGuideStepValue.entries.first()) {
            scrollState.animateScrollTo(0)
            return@LaunchedEffect
        }
        val start =
            guideStep?.cluster?.let { cluster -> clusterStarts[cluster] } ?: return@LaunchedEffect
        val padding = with(density) { 8.dp.toPx() }
        scrollState.animateScrollTo(
            (scrollState.value + start - viewportStart[0] - padding).roundToInt().coerceAtLeast(0)
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Neutral12,
        contentColor = Grey9A,
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .onGloballyPositioned { coordinates ->
                    viewportStart[0] = coordinates.positionInWindow().x
                }
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomizeClusterValue.entries.forEachIndexed { index, cluster ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 4.dp),
                        color = NeutralVariant30,
                    )
                }

                val step = CustomizeGuideStepValue.entries.first { it.cluster == cluster }
                Row(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInWindow()
                        clusterStarts[cluster] = position.x
                        onClusterPositioned(step, Rect(position, coordinates.size.toSize()))
                    },
                ) {
                    CustomizeTabValue.entries.filter { it.cluster == cluster }.forEach { tab ->
                        CustomizeTabItem(
                            tab = tab,
                            isSelected = tab == selectedTab,
                            onClick = { onIntent(CustomizeIntent.Panel.SelectTab(tab)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizeTabItem(
    tab: CustomizeTabValue,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Mint else Grey9A,
        label = "CustomizeTabContent",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MintAlpha12 else Color.Transparent,
        label = "CustomizeTabContainer",
    )

    Column(
        modifier = Modifier
            .widthIn(min = 64.dp)
            .onClick(shape = RoundedCornerShape12dp, action = onClick)
            .drawBehind {
                drawRoundRect(color = containerColor, cornerRadius = CornerRadius(12.dp.toPx()))
            }
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )

        Text(
            text = stringResource(tab.labelRes),
            modifier = Modifier.padding(top = 4.dp),
            color = contentColor,
            maxLines = 1,
            style = LabelMedium,
        )
    }
}
