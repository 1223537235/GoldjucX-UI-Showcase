package com.goldjucx.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.abs
import kotlin.math.roundToInt

data class GlassTab(val label: String, @DrawableRes val iconRes: Int)

@Composable
fun GlassBottomBar(
    tabs: List<GlassTab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    hazeState: HazeState? = null,
    hazeStyle: HazeStyle? = null,
    modifier: Modifier = Modifier
) {
    require(tabs.isNotEmpty())

    val tabWidth = 82.dp
    val tabHeight = 58.dp
    val density = LocalDensity.current
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val scope = rememberCoroutineScope()
    val currentSelectedTab by rememberUpdatedState(selectedTab)
    val haptic = rememberHaptic()

    val indicatorX = remember { Animatable(selectedTab * tabWidthPx) }

    LaunchedEffect(selectedTab) {
        indicatorX.animateTo(
            selectedTab * tabWidthPx,
            spring(dampingRatio = 0.7f, stiffness = 400f)
        )
    }

    val visualTab = ((indicatorX.value + tabWidthPx / 2) / tabWidthPx)
        .toInt().coerceIn(0, tabs.lastIndex)

    Box(
        modifier = modifier.padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val barShape = RoundedCornerShape(36.dp)
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = barShape,
                    ambientColor = Color(0x40000000),
                    spotColor = Color(0x30000000)
                )
                .clip(barShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startX = down.position.x
                        val slop = viewConfiguration.touchSlop

                        val curIndicatorX = indicatorX.value
                        val onIndicator = startX in curIndicatorX..(curIndicatorX + tabWidthPx)

                        var dragged = false
                        var prevX = startX

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            if (!change.pressed) {
                                if (dragged) {
                                    val snapTab = ((indicatorX.value + tabWidthPx / 2) / tabWidthPx)
                                        .toInt().coerceIn(0, tabs.lastIndex)
                                    scope.launch {
                                        indicatorX.animateTo(
                                            snapTab * tabWidthPx,
                                            spring(dampingRatio = 0.7f, stiffness = 400f)
                                        )
                                    }
                                    if (snapTab != currentSelectedTab) {
                                        haptic(GoldjucXHaptic.TAB_SLIDE)
                                        onTabSelected(snapTab)
                                    }
                                } else {
                                    val tappedTab = (startX / tabWidthPx).toInt().coerceIn(0, tabs.lastIndex)
                                    if (tappedTab != currentSelectedTab) {
                                        haptic(GoldjucXHaptic.TAB_SLIDE)
                                        onTabSelected(tappedTab)
                                    }
                                }
                                break
                            }

                            if (onIndicator) {
                                val dx = change.position.x - startX
                                if (!dragged && abs(dx) > slop) {
                                    dragged = true
                                    prevX = change.position.x
                                    change.consume()
                                }
                                if (dragged) {
                                    val delta = change.position.x - prevX
                                    prevX = change.position.x
                                    val newX = (indicatorX.value + delta)
                                        .coerceIn(0f, tabs.lastIndex * tabWidthPx)
                                    scope.launch { indicatorX.snapTo(newX) }
                                    change.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            // 磨砂背景：真实模糊或半透明白色兜底
            Box(
                Modifier
                    .matchParentSize()
                    .then(
                        if (hazeState != null) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = hazeStyle ?: HazeStyle(
                                    backgroundColor = Color.White,
                                    tint = HazeTint(Color.White.copy(alpha = 0.72f)),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0f
                                )
                            ) {
                                progressive = HazeProgressive.verticalGradient(
                                    startIntensity = 0.5f,
                                    endIntensity = 1f
                                )
                                inputScale = HazeInputScale.Auto
                            }
                        } else {
                            Modifier.background(Color.White.copy(alpha = 0.92f))
                        }
                    )
            )
            // 描边
            Box(
                Modifier
                    .matchParentSize()
                    .border(2.dp, Color.White.copy(alpha = 0.8f), barShape)
            )

            // 内容区 padding
            Box(Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                // 指示器（底层）— 恢复原始深色填充风格
                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorX.value.roundToInt(), 0) }
                        .width(tabWidth)
                        .height(tabHeight)
                        .background(Color(0x0F000000), RoundedCornerShape(32.dp))
                )

                // Tab 标签（上层）
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val active = index == visualTab
                        Column(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(tabHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp),
                                tint = if (active) Color.Black else Color.Black.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                                color = if (active) Color.Black else Color.Black.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
