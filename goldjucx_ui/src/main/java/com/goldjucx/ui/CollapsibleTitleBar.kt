package com.goldjucx.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.goldjucx.ui.R

/** 导航栏高度 */
val NAV_BAR_HEIGHT: Dp = 54.dp
/** 大标题区域高度 */
private val BIG_TITLE_HEIGHT: Dp = 58.dp
/** 标题栏展开时总高度 */
val TITLE_BAR_EXPANDED_HEIGHT: Dp = NAV_BAR_HEIGHT + BIG_TITLE_HEIGHT
/** 标题栏折叠时总高度（只有导航栏） */
val TITLE_BAR_COLLAPSED_HEIGHT: Dp = NAV_BAR_HEIGHT

/**
 * 折叠标题栏
 *
 * 返回当前标题栏实际高度（动画值），供外部内容区做 paddingTop。
 */
@Composable
fun CollapsibleTitleBar(
    title: String,
    scrollState: ScrollState? = null,
    lazyListState: LazyListState? = null,
    onBack: (() -> Unit)? = null,
    showBackButton: Boolean = true,
    backgroundColor: Color = GoldjucXColors.surfaceLow,
    hazeState: HazeState? = null,
    includeStatusBar: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
): Dp {
    val collapsed by remember(scrollState, lazyListState) {
        derivedStateOf {
            when {
                scrollState != null -> scrollState.value > 100
                lazyListState != null -> lazyListState.firstVisibleItemIndex > 0 ||
                        lazyListState.firstVisibleItemScrollOffset > 100
                else -> false
            }
        }
    }

    val bigTitleHeight by animateDpAsState(
        targetValue = if (collapsed) 0.dp else BIG_TITLE_HEIGHT,
        animationSpec = if (!collapsed) spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ) else tween(200),
        label = "bigTitleHeight"
    )

    val smallTitleAlpha by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "smallTitleAlpha"
    )
    val bigTitleAlpha by animateFloatAsState(
        targetValue = if (collapsed) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bigTitleAlpha"
    )

    // 小标题：折叠时从下方弹入（位移回弹）
    val smallTitleOffsetY by animateFloatAsState(
        targetValue = if (collapsed) 0f else 20f,
        animationSpec = if (collapsed) spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ) else tween(150),
        label = "smallTitleOffsetY"
    )
    // 大标题：展开时从上方弹入（位移回弹）
    val bigTitleOffsetY by animateFloatAsState(
        targetValue = if (collapsed) -15f else 0f,
        animationSpec = if (!collapsed) spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ) else tween(150),
        label = "bigTitleOffsetY"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        // Haze 毛玻璃层
        if (hazeState != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = backgroundColor,
                            tint = HazeTint(backgroundColor.copy(alpha = 0.78f)),
                            blurRadius = 36.dp,
                            noiseFactor = 0f
                        )
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            easing = androidx.compose.animation.core.CubicBezierEasing(0.85f, 0.0f, 0.8f, 1f)
                        )
                    }
            )
        } else {
            Box(Modifier.matchParentSize().background(backgroundColor))
        }

        Column(Modifier.fillMaxWidth().then(if (includeStatusBar) Modifier.statusBarsPadding() else Modifier)) {
        // 导航栏（始终显示）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(NAV_BAR_HEIGHT)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton && onBack != null) {
                CircleIconButton(
                    iconRes = R.drawable.ic_back,
                    contentDescription = stringResource(R.string.titlebar_back),
                    onClick = onBack
                )
            } else {
                Spacer(Modifier.size(44.dp))
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight(500), color = GoldjucXColors.onSurface),
                    modifier = Modifier.graphicsLayer {
                        alpha = smallTitleAlpha
                        translationY = smallTitleOffsetY
                    },
                    maxLines = 1
                )
            }

            if (trailingContent != null) trailingContent() else Spacer(Modifier.size(44.dp))
        }

        // 大标题区域 — 折叠时高度动画收缩到0
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bigTitleHeight)
                .graphicsLayer { alpha = bigTitleAlpha }
        ) {
            if (bigTitleHeight > 0.dp) {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 32.sp, color = GoldjucXColors.onSurface),
                    modifier = Modifier
                        .padding(start = 26.dp, end = 26.dp, top = 8.dp, bottom = 10.dp)
                        .graphicsLayer {
                            translationY = bigTitleOffsetY
                        },
                    maxLines = 1
                )
            }
        }
        // 折叠态底部延伸：给渐进模糊提供过渡空间
        val blurOverhang by animateDpAsState(
            targetValue = if (collapsed) 24.dp else 0.dp,
            animationSpec = tween(200),
            label = "blurOverhang"
        )
        Spacer(Modifier.height(blurOverhang))
        } // Column
    } // Box

    val statusBarHeight = if (includeStatusBar) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp
    return statusBarHeight + NAV_BAR_HEIGHT + bigTitleHeight
}
