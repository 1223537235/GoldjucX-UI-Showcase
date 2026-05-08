package com.goldjucx.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 抽屉堆栈状态：追踪当前活跃的抽屉层数，实现叠加效果。
 * 第一个抽屉 push 得到 level=0，第二个得到 level=1，以此类推。
 */
class DrawerStackState {
    var depth by mutableIntStateOf(0)
        private set
    // 最上层抽屉的关闭/拖拽进度：0=完全展开, 1=完全关闭
    // 仅在入场动画完成后由顶层更新，底层读取实现拖拽联动
    var topDismissProgress by mutableFloatStateOf(0f)
    // dismissAll：递增触发所有抽屉连续关闭
    var dismissAllSignal by mutableIntStateOf(0)
        private set
    var dismissAllDepth by mutableIntStateOf(0)
        private set
    var isDismissingAll by mutableStateOf(false)
        private set
    fun push(): Int { val level = depth; depth++; topDismissProgress = 0f; return level }
    fun pop() { depth--; if (depth <= 0) isDismissingAll = false }
    fun dismissAll() { isDismissingAll = true; dismissAllDepth = depth; dismissAllSignal++ }
}

val LocalDrawerStack = compositionLocalOf { DrawerStackState() }

/**
 * 面板背景模式。
 * - [Gray] 灰色实色（默认）
 * - [White] 白色实色
 * - [Blur] 磨砂玻璃（面板半透明 + 截图模糊底层内容）
 */
sealed class SheetBackground {
    data object Gray : SheetBackground()
    data object White : SheetBackground()
    data class Blur(val blurRadius: Dp = 40.dp) : SheetBackground()
}

@Composable
fun BottomSheetDrawer(
    onClose: () -> Unit,
    showHandle: Boolean = true,
    background: SheetBackground = SheetBackground.Gray,
    heightFraction: Float = 0.93f,
    @DrawableRes headerIcon: Int? = R.drawable.ic_back,
    title: String? = null,
    subtitle: String? = null,
    dismissOnPullDown: Boolean = true,
    trailingIcons: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.(animateOut: suspend () -> Unit) -> Unit
) {
    val sheetColor = when (background) {
        SheetBackground.Gray -> GoldjucXColors.surfaceLow
        SheetBackground.White -> Color.White
        is SheetBackground.Blur -> Color.White
    }
    val blurSheet = background is SheetBackground.Blur
    val blurSheetRadius = if (background is SheetBackground.Blur) background.blurRadius else 40.dp
    val blurBackground = true
    // ─── 堆栈注册 ───
    val stack = LocalDrawerStack.current
    val haptic = rememberHaptic()
    val myLevel = remember { stack.push() }
    DisposableEffect(Unit) { onDispose { stack.pop() } }
    val isPushedBack by remember { derivedStateOf { stack.depth > myLevel + 1 } }
    val pushedDepth by remember { derivedStateOf { (stack.depth - myLevel - 1).coerceAtLeast(0) } }
    // 自己正上方就是顶层（拖拽联动只对这一层生效）
    val isDirectlyBelowTop by remember { derivedStateOf { stack.depth == myLevel + 2 } }

    // ─── 被压视觉动画：直接 animate 目标值，不用公式 ───
    val baseScale = when (pushedDepth) { 0 -> 1f; 1 -> 0.92f; else -> 0.86f }
    val baseTranslateY = when (pushedDepth) { 0 -> 0f; 1 -> -50f; else -> -80f }
    val baseBlur = when (pushedDepth) { 0 -> 0f; 1 -> 4f; else -> 6f }
    val baseDim = when (pushedDepth) { 0 -> 0f; 1 -> 0.10f; else -> 0.15f }
    // 联动恢复目标（减一层的位置）
    val oneUpScale = when (pushedDepth) { 0 -> 1f; 1 -> 1f; else -> 0.92f }
    val oneUpTranslateY = when (pushedDepth) { 0 -> 0f; 1 -> 0f; else -> -50f }
    // 描边：顶层显示，被压隐藏，联动恢复时渐显
    val baseBorderAlpha = when (pushedDepth) { 0 -> 0.8f; else -> 0f }
    val oneUpBorderAlpha = 0.8f

    val scaleAnim = remember { Animatable(1f) }
    val translateYAnim = remember { Animatable(0f) }
    val blurAnim = remember { Animatable(0f) }
    val dimAnim = remember { Animatable(0f) }
    val borderAlphaAnim = remember { Animatable(0.8f) }

    // 统一动画管理：animateTo 和 snapshotFlow 并行运行
    // 当上层开始关闭时，snapshotFlow 立即跟踪进度（snapTo 会中断 animateTo）
    // 当 isDirectlyBelowTop 变为 false（上层 dispose），effect 被取消，
    // Animatable 保留最后 snap 的值，新 effect 从该值 animateTo 目标，无跳变。
    LaunchedEffect(isDirectlyBelowTop, baseScale, baseTranslateY, baseBlur, baseDim, baseBorderAlpha) {
        if (stack.isDismissingAll) return@LaunchedEffect
        if (isDirectlyBelowTop) {
            // 并行：层级过渡动画 + 联动跟踪同时启动
            launch { scaleAnim.animateTo(baseScale, tween(350)) }
            launch { translateYAnim.animateTo(baseTranslateY, tween(350)) }
            launch { blurAnim.animateTo(baseBlur, tween(350)) }
            launch { dimAnim.animateTo(baseDim, tween(350)) }
            launch { borderAlphaAnim.animateTo(baseBorderAlpha, tween(350)) }
            // 不阻塞等待 animateTo 完成，立即开始跟踪
            // 当 progress > 0 时 snapTo 会自动中断正在运行的 animateTo
            snapshotFlow { stack.topDismissProgress }.collect { progress ->
                if (progress > 0.01f) {
                    scaleAnim.snapTo(baseScale + progress * (oneUpScale - baseScale))
                    translateYAnim.snapTo(baseTranslateY + progress * (oneUpTranslateY - baseTranslateY))
                    blurAnim.snapTo(baseBlur * (1f - progress))
                    dimAnim.snapTo(baseDim * (1f - progress))
                    borderAlphaAnim.snapTo(baseBorderAlpha + progress * (oneUpBorderAlpha - baseBorderAlpha))
                }
            }
        } else {
            launch { scaleAnim.animateTo(baseScale, tween(350)) }
            launch { translateYAnim.animateTo(baseTranslateY, tween(350)) }
            launch { blurAnim.animateTo(baseBlur, tween(350)) }
            launch { dimAnim.animateTo(baseDim, tween(350)) }
            launch { borderAlphaAnim.animateTo(baseBorderAlpha, tween(350)) }
        }
    }

    // 性能关键：borderAlpha 仍需组合层读取（驱动 border modifier）
    // scale/translateY/blur/dim 全部移到 graphicsLayer/drawBehind 内读取，避免重组
    val finalBorderAlpha = borderAlphaAnim.value

    val offsetY = remember { Animatable(3000f) }
    var sheetHeight by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    var dismissing by remember { mutableStateOf(false) }
    var entryComplete by remember { mutableStateOf(false) }

    // 入场完成后才更新 topDismissProgress（避免入场动画干扰底层联动）
    val myDismissProgress = if (sheetHeight > 0f) (offsetY.value / sheetHeight).coerceIn(0f, 1f) else 0f
    SideEffect {
        if (stack.depth == myLevel + 1 && entryComplete) {
            stack.topDismissProgress = myDismissProgress
        }
    }

    val view = LocalView.current
    val activity = view.context as? android.app.Activity

    val useSystemBlur = remember {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            activity?.window?.attributes?.flags?.and(
                android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            ) != 0
    }

    val needSnapshot = blurSheet
    var snapshotBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    val animateOut: suspend () -> Unit = {
        if (!dismissing && sheetHeight > 0f) {
            dismissing = true
            val speedMultiplier = 1f + (myLevel * 0.3f)
            val duration = (450 / speedMultiplier).toInt()
            offsetY.animateTo(sheetHeight, tween(duration))
        }
    }

    val dismissAndClose: suspend () -> Unit = {
        animateOut()
        onClose()
    }

    BackHandler(enabled = !isPushedBack) {
        scope.launch { dismissAndClose() }
    }

    // dismissAll：退出动画最多播三层（逐层加速），超出的等动画结束后静默移除
    val initialDismissSignal = remember { stack.dismissAllSignal }
    LaunchedEffect(stack.dismissAllSignal) {
        if (stack.dismissAllSignal > initialDismissSignal && !dismissing) {
            val posFromTop = stack.dismissAllDepth - 1 - myLevel
            if (posFromTop >= 3) {
                // 立即隐藏，等前三层动画播完后再移除树
                dismissing = true
                if (sheetHeight > 0f) offsetY.snapTo(sheetHeight)
                kotlinx.coroutines.delay(450L)
                onClose()
            } else {
                var totalDelay = 0L
                for (i in 1..posFromTop) { totalDelay += (120L - i * 30L).coerceAtLeast(40L) }
                kotlinx.coroutines.delay(totalDelay)
                val duration = 400 - posFromTop * 80
                dismissing = true
                offsetY.animateTo(sheetHeight, tween(duration))
                onClose()
            }
        }
    }

    LaunchedEffect(sheetHeight) {
        if (sheetHeight > 0f) {
            offsetY.snapTo(sheetHeight)
            if (needSnapshot && snapshotBitmap == null && activity != null && view.width > 0) {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                try {
                    val success = suspendCancellableCoroutine<Boolean> { cont ->
                        android.view.PixelCopy.request(
                            activity.window, bitmap,
                            { result -> cont.resumeWith(Result.success(result == android.view.PixelCopy.SUCCESS)) },
                            android.os.Handler(android.os.Looper.getMainLooper())
                        )
                    }
                    if (success) snapshotBitmap = bitmap.asImageBitmap()
                } catch (_: Exception) { }
            }
            offsetY.animateTo(0f, tween(450))
            entryComplete = true
        }
    }

    val scrimAlpha = if (sheetHeight > 0f) {
        (1f - (offsetY.value / sheetHeight).coerceIn(0f, 1f)) * 0.4f
    } else 0f

    if (blurBackground && useSystemBlur && myLevel == 0) {
        val progress = if (sheetHeight > 0f) (1f - (offsetY.value / sheetHeight).coerceIn(0f, 1f)) else 0f
        LaunchedEffect(progress) {
            activity?.window?.let { win ->
                win.attributes = win.attributes.also {
                    it.blurBehindRadius = (progress * 50).toInt()
                }
            }
        }
    }

    // 入场前隐藏（避免 haze/blur 未就绪时闪黑）+ 关闭动画完成后隐藏
    val isFullyDismissed = dismissing && sheetHeight > 0f && offsetY.value >= sheetHeight - 1f
    val isBeforeEntry = sheetHeight == 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (isFullyDismissed || isBeforeEntry) 0f else 1f }
            .then(
                if (!isPushedBack && !isFullyDismissed) Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { scope.launch { dismissAndClose() } }
                else Modifier
            )
    ) {
        // 黑色蒙层：每层都渲染
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0, 0, 0, (scrimAlpha * 255).toInt().coerceIn(0, 255)))
        )

        // ─── 抽屉面板 ───
        val sheetShape = RoundedCornerShape(36.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction.coerceIn(0.1f, 0.94f))
                .align(Alignment.BottomCenter)
                .onSizeChanged { sheetHeight = it.height.toFloat() }
                .graphicsLayer {
                    val s = scaleAnim.value
                    val ty = translateYAnim.value
                    translationY = offsetY.value.coerceAtLeast(0f) + ty
                    scaleX = s
                    scaleY = s
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                    val blur = blurAnim.value
                    if (blur > 0.5f) {
                        val blurPx = blur * density
                        renderEffect = androidx.compose.ui.graphics.BlurEffect(
                            blurPx, blurPx, androidx.compose.ui.graphics.TileMode.Decal
                        )
                    }
                }
                .clip(sheetShape)
                .border(2.dp, Color.White.copy(alpha = finalBorderAlpha), sheetShape)
                .then(
                    if (!isPushedBack) Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* consume */ }
                    else Modifier
                )
        ) {
            // 底色
            if (blurSheet) {
                // 磨砂玻璃：截图模糊层 + 半透明白色 tint
                if (snapshotBitmap != null) {
                    Image(
                        bitmap = snapshotBitmap!!,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { translationY = -offsetY.value }
                            .blur(blurSheetRadius),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.BottomCenter
                    )
                }
                Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.78f)))
            } else {
                Box(Modifier.fillMaxSize().background(sheetColor))
            }

            // 被压暗色遮罩（drawBehind 避免重组）
            Box(Modifier.fillMaxSize().drawBehind {
                val dim = dimAnim.value
                if (dim > 0.005f) {
                    drawRect(Color.Black.copy(alpha = dim))
                }
            })

            // Sheet content
            val contentScrollState = rememberScrollState()
            val hasHeader = headerIcon != null || title != null || trailingIcons != null
            val headerHeight = (if (showHandle) 24.dp else 0.dp) + (if (hasHeader) 56.dp else 0.dp)
            val hazeState = remember { HazeState() }

            Box(Modifier.fillMaxSize()) {
                // 内容区（可滚动，作为 hazeSource）
                Column(
                    Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                        .then(
                            if (dismissOnPullDown && !isPushedBack) Modifier.pointerInput(contentScrollState) {
                                var lastDragVelocity = 0f
                                var hitBoundary = false
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        hitBoundary = false
                                        scope.launch {
                                            val fastSwipe = lastDragVelocity > 15f
                                            val distanceRatio = offsetY.value / sheetHeight
                                            if ((fastSwipe && distanceRatio > 0.15f) || distanceRatio > 0.4f) {
                                                dismissAndClose()
                                            } else {
                                                offsetY.animateTo(0f, tween(350))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        hitBoundary = false
                                        scope.launch { offsetY.animateTo(0f, tween(350)) }
                                    },
                                    onVerticalDrag = { _, dragAmount ->
                                        lastDragVelocity = dragAmount
                                        val atTop = contentScrollState.value == 0
                                        if ((dragAmount > 0 && atTop) || offsetY.value > 0f) {
                                            hitBoundary = false
                                            val newY = (offsetY.value + dragAmount).coerceAtLeast(0f)
                                            scope.launch { offsetY.snapTo(newY) }
                                        } else if (dragAmount < 0 && offsetY.value == 0f && atTop) {
                                            if (!hitBoundary) { hitBoundary = true; haptic(GoldjucXHaptic.BOUNDARY) }
                                        }
                                    }
                                )
                            } else Modifier
                        )
                        .verticalScroll(contentScrollState)
                        .padding(top = headerHeight + 14.dp)
                ) {
                    content(animateOut)
                    Spacer(Modifier.height(50.dp))
                }

                // 标题栏浮动层（渐进式模糊）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .zIndex(10f)
                ) {
                    // Haze 渐进模糊背景
                    Box(
                        Modifier
                            .matchParentSize()
                            .hazeEffect(
                                state = hazeState,
                                style = HazeStyle(
                                    backgroundColor = if (blurSheet) Color.Transparent else sheetColor,
                                    tint = HazeTint(
                                        if (blurSheet) Color.White.copy(alpha = 0.88f)
                                        else sheetColor.copy(alpha = 0.85f)
                                    ),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0f
                                )
                            ) {
                                progressive = HazeProgressive.verticalGradient(
                                    startIntensity = 1f,
                                    endIntensity = 0f,
                                    easing = androidx.compose.animation.core.CubicBezierEasing(0.65f, 0f, 0.75f, 1f)
                                )
                            }
                    )

                    Column(Modifier.fillMaxWidth()) {
                        if (showHandle) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .then(
                                        if (!isPushedBack) Modifier.pointerInput(Unit) {
                                            var hitBoundary = false
                                            detectVerticalDragGestures(
                                                onDragEnd = {
                                                    hitBoundary = false
                                                    scope.launch {
                                                        if (offsetY.value > sheetHeight * 0.5f) {
                                                            dismissAndClose()
                                                        } else {
                                                            offsetY.animateTo(0f, tween(350))
                                                        }
                                                    }
                                                },
                                                onDragCancel = {
                                                    hitBoundary = false
                                                    scope.launch { offsetY.animateTo(0f, tween(350)) }
                                                },
                                                onVerticalDrag = { _, dragAmount ->
                                                    val newY = (offsetY.value + dragAmount).coerceAtLeast(0f)
                                                    if (newY == 0f && dragAmount < 0 && offsetY.value == 0f) {
                                                        if (!hitBoundary) { hitBoundary = true; haptic(GoldjucXHaptic.BOUNDARY) }
                                                    } else {
                                                        hitBoundary = false
                                                    }
                                                    scope.launch { offsetY.snapTo(newY) }
                                                }
                                            )
                                        }
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    Modifier
                                        .width(60.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(GoldjucXColors.outline)
                                )
                            }
                        }

                        if (hasHeader) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .defaultMinSize(minHeight = 44.dp)
                            ) {
                                if (headerIcon != null) {
                                    CircleIconButton(
                                        iconRes = headerIcon,
                                        contentDescription = "关闭",
                                        onClick = { scope.launch { dismissAndClose() } },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }
                                if (title != null || subtitle != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 56.dp)
                                            .align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (title != null) {
                                            Text(
                                                title,
                                                style = TextStyle(
                                                    fontSize = 20.sp,
                                                    lineHeight = 20.sp,
                                                    color = GoldjucXColors.onSurface,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                        if (subtitle != null) {
                                            Text(
                                                subtitle,
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    lineHeight = 14.sp,
                                                    color = GoldjucXColors.onSurfaceTertiary,
                                                    textAlign = TextAlign.Center
                                                )
                                            )
                                        }
                                    }
                                }
                                if (trailingIcons != null) {
                                    Row(
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        trailingIcons()
                                    }
                                }
                            }
                        }

                        // 渐进模糊过渡延伸区
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
