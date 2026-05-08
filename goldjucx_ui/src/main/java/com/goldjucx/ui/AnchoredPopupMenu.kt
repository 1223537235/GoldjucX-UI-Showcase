package com.goldjucx.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.goldjucx.ui.R

/**
 * 自包含的近手弹窗选择器。
 *
 * 内置锚点定位逻辑：自动测量 anchor 内容的窗口坐标，
 * 智能判断弹窗方向（锚点偏低向上展开，偏高向下展开）。
 * 通过 Dialog 渲染到窗口层级，蒙层覆盖状态栏和导航栏。
 */
@Composable
fun AnchoredPopupMenuBox(
    options: List<String>,
    selected: String,
    expanded: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    checkIconRes: Int = R.drawable.ic_check,
    anchor: @Composable () -> Unit
) {
    var anchorRight by remember { mutableFloatStateOf(0f) }
    var anchorTop by remember { mutableFloatStateOf(0f) }
    var anchorBottom by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier.onGloballyPositioned { coords ->
            val pos = coords.positionInWindow()
            val size = coords.size
            anchorRight = pos.x + size.width
            anchorTop = pos.y
            anchorBottom = pos.y + size.height
        }
    ) {
        anchor()
    }

    if (expanded) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)

            AnchoredPopupMenuContent(
                options = options,
                selected = selected,
                anchorRight = anchorRight,
                anchorBottom = anchorBottom,
                anchorTop = anchorTop,
                onSelect = onSelect,
                onDismiss = onDismiss,
                checkIconRes = checkIconRes
            )
        }
    }
}

@Composable
private fun AnchoredPopupMenu(
    options: List<String>,
    selected: String,
    anchorRight: Float,
    anchorBottom: Float,
    anchorTop: Float = anchorBottom - 56f,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    checkIconRes: Int = R.drawable.ic_check
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)

        AnchoredPopupMenuContent(
            options = options,
            selected = selected,
            anchorRight = anchorRight,
            anchorBottom = anchorBottom,
            anchorTop = anchorTop,
            onSelect = onSelect,
            onDismiss = onDismiss,
            checkIconRes = checkIconRes
        )
    }
}

@Composable
private fun AnchoredPopupMenuContent(
    options: List<String>,
    selected: String,
    anchorRight: Float,
    anchorBottom: Float,
    anchorTop: Float,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    checkIconRes: Int
) {
    val haptic = rememberHaptic()
    var showing by remember { mutableStateOf(false) }
    var dismissing by remember { mutableStateOf(false) }
    var internalSelected by remember { mutableStateOf(selected) }
    var pendingSelection by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    val anchorCenterY = (anchorTop + anchorBottom) / 2f
    val expandUpward = anchorCenterY > screenHeightPx / 2f

    LaunchedEffect(Unit) { showing = true }

    BackHandler { dismissing = true }

    val enterScale by animateFloatAsState(
        targetValue = if (showing) 1f else 0f,
        animationSpec = tween(350), label = "enterScale"
    )
    val enterAlpha by animateFloatAsState(
        targetValue = if (showing) 1f else 0f,
        animationSpec = tween(300), label = "enterAlpha"
    )

    val exitScale by animateFloatAsState(
        targetValue = if (dismissing) 0f else 1f,
        animationSpec = tween(400), label = "exitScale"
    )
    val exitAlpha by animateFloatAsState(
        targetValue = if (dismissing) 0f else 1f,
        animationSpec = tween(400), label = "exitAlpha"
    )

    LaunchedEffect(dismissing) {
        if (dismissing) {
            kotlinx.coroutines.delay(400)
            pendingSelection?.let { onSelect(it) }
            onDismiss()
        }
    }

    val finalScale = enterScale * exitScale
    val finalAlpha = enterAlpha * exitAlpha
    val scrimAlpha = finalAlpha * 0.15f

    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    LaunchedEffect(finalAlpha) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && dialogWindow != null) {
            val radius = (finalAlpha * 50).toInt()
            dialogWindow.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            dialogWindow.attributes = dialogWindow.attributes?.apply { blurBehindRadius = radius }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { dismissing = true }
            )
    ) {
        // 蒙层（跟随缩放动画联动透明度）
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { alpha = scrimAlpha / 0.15f }
                .background(Color(0, 0, 0, 38))
        )

        // 弹窗
        val menuWidth = with(density) { 200.dp.toPx() }
        val inset = with(density) { 10.dp.toPx() }
        var menuHeightPx by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .offset {
                    val x = (anchorRight - menuWidth - 4.dp.toPx()).toInt()
                    val y = if (expandUpward) {
                        (anchorTop - menuHeightPx + inset).toInt()
                    } else {
                        (anchorBottom - inset).toInt()
                    }
                    IntOffset(x, y)
                }
                .onSizeChanged { menuHeightPx = it.height }
                .graphicsLayer {
                    this.alpha = finalAlpha
                    scaleX = finalScale
                    scaleY = finalScale
                    transformOrigin = if (expandUpward) {
                        TransformOrigin(1f, 1f)
                    } else {
                        TransformOrigin(1f, 0f)
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            val scrollState = rememberScrollState()
            val scrollBarAlpha = remember { Animatable(0f) }
            val isScrollable = scrollState.maxValue > 0

            LaunchedEffect(scrollState.isScrollInProgress) {
                if (scrollState.isScrollInProgress) {
                    scrollBarAlpha.animateTo(1f, tween(150))
                } else {
                    kotlinx.coroutines.delay(800)
                    scrollBarAlpha.animateTo(0f, tween(500))
                }
            }

            Column(
                modifier = Modifier
                    .width(200.dp)
                    .heightIn(max = 240.dp)
                    .shadow(40.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x29000000), spotColor = Color(0x29000000))
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.72f))
                    .padding(8.dp)
                    .verticalScroll(scrollState)
                    .drawWithContent {
                        drawContent()
                        if (isScrollable && scrollBarAlpha.value > 0f) {
                            val viewportH = size.height
                            val contentH = viewportH + scrollState.maxValue
                            val ratio = viewportH / contentH
                            val thumbH = (ratio * viewportH).coerceIn(18.dp.toPx(), viewportH * 0.35f)
                            val trackH = viewportH - 12.dp.toPx()
                            val thumbOffset = 6.dp.toPx() + (scrollState.value.toFloat() / scrollState.maxValue) * (trackH - thumbH)
                            val barWidth = 2.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFFCCCCCC).copy(alpha = 0.35f * scrollBarAlpha.value),
                                topLeft = Offset(size.width - 4.dp.toPx(), thumbOffset),
                                size = Size(barWidth, thumbH),
                                cornerRadius = CornerRadius(barWidth)
                            )
                        }
                    }
            ) {
                options.forEach { option ->
                    val isSelected = option == internalSelected
                    Row(
                        modifier = Modifier
                            .width(184.dp)
                            .defaultMinSize(minHeight = 44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    internalSelected = option
                                    pendingSelection = option
                                    haptic(GoldjucXHaptic.GEAR_LIGHT)
                                    dismissing = true
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            option,
                            style = TextStyle(fontSize = 16.sp, color = if (isSelected) GoldjucXColors.primary else GoldjucXColors.onSurface),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painterResource(checkIconRes), null,
                            Modifier.size(24.dp).graphicsLayer { this.alpha = if (isSelected) 1f else 0f },
                            tint = GoldjucXColors.primary
                        )
                    }
                }
            }
        }
    }
}
