package com.decoutkhanqindev.custom_aod.presentation.screens.customize.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeDraftUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.customize.CustomizeTabValue
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeDecorState
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.state.CustomizeIntent
import com.decoutkhanqindev.custom_aod.presentation.theme.GreyED
import com.decoutkhanqindev.custom_aod.presentation.theme.Neutral12
import com.decoutkhanqindev.custom_aod.presentation.theme.RoundedCornerTopShape16dp
import com.decoutkhanqindev.custom_aod.presentation.theme.TitleMedium

@Composable
fun CustomizePanelHost(
    selectedTab: CustomizeTabValue?,
    draft: CustomizeDraftUiModel,
    decor: CustomizeDecorState,
    onIntent: (CustomizeIntent) -> Unit,
) {
    var lastTab by remember { mutableStateOf(selectedTab ?: CustomizeTabValue.entries.first()) }

    SideEffect(selectedTab) {
        selectedTab?.let { tab -> lastTab = tab }
    }

    AnimatedVisibility(
        visible = selectedTab != null,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = 0.9f,
                stiffness = 700f,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            initialOffsetY = { height -> height },
        ),
        exit = slideOutVertically(
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = 3800f,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            targetOffsetY = { height -> height },
        ),
        label = "CustomizePanel",
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerTopShape16dp,
            color = Neutral12,
            contentColor = GreyED,
        ) {
            AnimatedContent(
                targetState = selectedTab ?: lastTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "CustomizePanelContent",
            ) { tab ->
                Column(modifier = Modifier.heightIn(max = 360.dp)) {
                    Text(
                        text = stringResource(tab.labelRes),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center,
                        style = TitleMedium,
                    )

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                    ) {
                        CustomizePanel(
                            tab = tab,
                            draft = draft,
                            decor = decor,
                            onIntent = onIntent,
                        )
                    }
                }
            }
        }
    }
}
