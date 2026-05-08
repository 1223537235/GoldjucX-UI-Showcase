package com.goldjucx.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.goldjucx.ui.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

/** 按压背景色（浅灰） */
private val PressedBgColor = Color(0xFFF0F0F0)

/**
 * 按压效果：行缩放 + 压黑层覆盖整行（公开版本，供外部使用）
 */
@Composable
fun Modifier.pressDimPublic(onClick: () -> Unit): Modifier = pressDim(onClick)

/**
 * 按压效果：行缩放 + 压黑层覆盖整行
 */
@Composable
private fun Modifier.pressDim(onClick: () -> Unit): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(if (pressed) 80 else 200),
        label = "pressScale"
    )
    val dimAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.06f else 0f,
        animationSpec = tween(if (pressed) 60 else 250),
        label = "pressDim"
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .drawWithContent {
            drawContent()
            drawRect(Color.Black.copy(alpha = dimAlpha))
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    try { awaitRelease() } finally { pressed = false }
                },
                onTap = { onClick() }
            )
        }
}

/**
 * GoldjucX 风格设置页面布局组件集
 *
 * ## 组件列表
 * - `SettingsCard` — 圆角卡片容器
 * - `SettingsSwitchRow` — 带开关的设置行（标题+副标题+Switch）
 * - `SettingsNavigateRow` — 带箭头的导航行（标题+右侧值+箭头）
 * - `SettingsSectionTitle` — 分组小标题
 * - `ContinuousCardList` — 连续卡片列表
 * - `SelectableOptionRow` — 可选选项行（带勾选图标）
 * - `CollapsibleSection` — 可折叠区域
 * - `LottieHeroSection` — Lottie 动画 + 说明文案区域
 * - `SettingsValueRow` — 带值+箭头的设置行
 */

/** 圆角卡片容器（单项或自由布局），自带 horizontal=12dp, top=10dp */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor),
        content = content
    )
}

/** 列表卡片：自动处理圆角（首行顶圆角，末行底圆角，中间无圆角），内部包裹 clip+background */
@Composable
fun <T> SettingsCard(
    items: List<T>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    enabled: Boolean = true,
    cornerRadius: Dp = 20.dp,
    itemContent: @Composable (item: T) -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.45f,
        animationSpec = tween(300), label = "cardListAlpha"
    )
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp)
            .graphicsLayer { this.alpha = alpha }
    ) {
        items.forEachIndexed { index, item ->
            val shape = cardShape(index, items.size, cornerRadius)
            Box(Modifier.fillMaxWidth().clip(shape).background(backgroundColor)) {
                itemContent(item)
            }
        }
    }
}

/** 纯文本设置行（标题+副标题，无开关/箭头） */
@Composable
fun SettingsTextRow(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 70.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .graphicsLayer { this.alpha = alpha },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
            if (subtitle != null) {
                Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
            }
        }
    }
}

/** 带开关的设置行 */
@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 70.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
            if (subtitle != null) {
                Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
            }
        }
        GoldjucXSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** 带箭头的导航行 */
@Composable
fun SettingsNavigateRow(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressDim(onClick)
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface),
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (value != null) {
                Text(value, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary))
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(10.dp, 16.dp),
                tint = Color.Unspecified
            )
        }
    }
}

/** 带自定义右侧内容的可点击行（如加号按钮） */
@Composable
fun SettingsActionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressDim(onClick)
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface), modifier = Modifier.weight(1f))
        trailingContent()
    }
}

/** 分组小标题，上方留出与卡片的间距，下方留出与列表的间距 */
@Composable
fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        title,
        style = TextStyle(fontSize = 13.sp, color = GoldjucXColors.gray),
        modifier = modifier.padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 4.dp)
    )
}

/**
 * 行间长文本：用于卡片之间的说明性文字（隐私声明、权限提示等）。
 * 颜色比 SettingsSectionTitle 更淡（onSurfaceQuaternary 40% black），13sp。
 */
@Composable
fun SettingsHintText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text,
        style = TextStyle(fontSize = 13.sp, color = GoldjucXColors.onSurfaceQuaternary),
        modifier = modifier.padding(horizontal = 28.dp, vertical = 8.dp)
    )
}

/**
 * 图文介绍文本：页面顶部头图下方的标题+描述区域。
 * 标题居中 17sp onSurface，描述居中 14sp onSurfaceQuaternary（比正文淡）。
 * 支持 AnnotatedString 描述（用于蓝色链接等）。
 */
@Composable
fun SettingsHeroSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = TextStyle(fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight(380),
            color = GoldjucXColors.onSurface, textAlign = TextAlign.Center))
        Text(description, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary,
            textAlign = TextAlign.Start), modifier = Modifier.fillMaxWidth())
    }
}

/** SettingsHeroSection 带 AnnotatedString 描述（支持蓝色链接等） */
@Composable
fun SettingsHeroSection(
    title: String,
    description: androidx.compose.ui.text.AnnotatedString,
    onDescriptionClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = TextStyle(fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight(380),
            color = GoldjucXColors.onSurface, textAlign = TextAlign.Center))
        androidx.compose.foundation.text.ClickableText(
            text = description,
            style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary, textAlign = TextAlign.Start),
            modifier = Modifier.fillMaxWidth(),
            onClick = onDescriptionClick
        )
    }
}


/** 可选选项行（带勾选图标），用于地点类型多选、分类选择等场景。 */
@Composable
fun SelectableOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth()
            .clip(shape).background(Color.White)
            .pressDim(onClick)
            .defaultMinSize(minHeight = 56.dp).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface), modifier = Modifier.weight(1f))
        if (isSelected) Icon(painterResource(R.drawable.ic_check), null, Modifier.size(20.dp), tint = GoldjucXColors.primary)
    }
}

/** 多选选项行（带填充圆形勾选图标），用于应用分类多选等场景。 */
@Composable
fun CheckableOptionRow(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth()
            .clip(shape).background(Color.White)
            .pressDim(onClick)
            .defaultMinSize(minHeight = 56.dp).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface), modifier = Modifier.weight(1f))
        Icon(
            painterResource(if (isChecked) R.drawable.ic_check_circle else R.drawable.ic_uncheck_circle),
            null, Modifier.size(24.dp), tint = Color.Unspecified
        )
    }
}

/** 可折叠区域：标题行带箭头旋转动画，内容区带展开/收起动画，内部自动处理底部圆角和背景。 */
@Composable
fun CollapsibleSection(
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 10.dp)) {
        val titleShape = if (expanded) RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) else RoundedCornerShape(20.dp)
        Row(
            Modifier.fillMaxWidth()
                .clip(titleShape).background(backgroundColor)
                .pressDim(onToggle)
                .defaultMinSize(minHeight = 70.dp).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
                if (subtitle != null) Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
            }
            Icon(painterResource(R.drawable.ic_arrow_right), null,
                Modifier.size(10.dp, 16.dp).graphicsLayer { rotationZ = if (expanded) 90f else 0f },
                tint = Color.Unspecified)
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(backgroundColor),
                content = content
            )
        }
    }
}

/** Lottie 动画 + 说明文案区域 */
@Composable
fun ColumnScope.LottieHeroSection(
    animationAsset: String,
    title: String,
    description: AnnotatedString,
    onDescriptionClick: (Int) -> Unit = {},
    animationHeight: Dp = 280.dp
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationAsset))
    var animPlaying by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(500); animPlaying = true }
    val progress by animateLottieCompositionAsState(composition = composition, isPlaying = animPlaying, iterations = 1)
    LottieAnimation(composition = composition, progress = { progress },
        modifier = Modifier.height(animationHeight).align(Alignment.CenterHorizontally))
    Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 4.dp)) {
        Text(title, style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight(380), color = GoldjucXColors.onSurface, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
        ClickableText(text = description, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary, textAlign = TextAlign.Start),
            modifier = Modifier.fillMaxWidth(), onClick = onDescriptionClick)
    }
}

/** 带值+箭头的设置行（用于窥视感知、启用类型等需要弹窗选择的场景） */
@Composable
fun SettingsValueRow(
    title: String,
    subtitle: String? = null,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth()
            .pressDim(onClick)
            .defaultMinSize(minHeight = 70.dp).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
            if (subtitle != null) Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(value, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary))
            Icon(painterResource(R.drawable.ic_arrow_updown), null, Modifier.size(10.dp, 16.dp), tint = Color.Unspecified)
        }
    }
}

/**
 * 带图标的开关行：左侧自定义图标 + 标题/副标题 + 右侧 Switch
 * 用于 AppPageDetailScreen 等需要 App 图标 + 开关的场景
 */
@Composable
fun SettingsIconSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 70.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        leadingIcon()
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
            if (subtitle != null) {
                Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
            }
        }
        GoldjucXSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * 带图标的导航行：左侧自定义图标 + 标题/副标题 + 右侧值+箭头
 * 用于 PageProtectionScreen 等需要 App 图标 + 导航箭头的场景
 */
@Composable
fun SettingsIconNavigateRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressDim(onClick)
            .defaultMinSize(minHeight = 70.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        leadingIcon()
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
            if (subtitle != null) {
                Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (value != null) {
                Text(value, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary))
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(10.dp, 16.dp),
                tint = Color.Unspecified
            )
        }
    }
}

/**
 * 统计卡片：用于展示数字统计（如"258次"）
 * 用于 PrivacySecurityScreen 权限使用记录等场景
 */
@Composable
fun StatCard(
    label: String,
    count: String,
    unit: String = stringResource(R.string.settings_stat_unit),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column {
            Text(label, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight(380), color = GoldjucXColors.onSurfaceTertiary))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(count, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, color = GoldjucXColors.onSurface))
                Text(unit, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight(380), color = GoldjucXColors.onSurfaceQuaternary))
            }
        }
    }
}

/**
 * 选择列表底部弹窗：标题 + 可选项列表 + 取消按钮
 * 用于 AppProtectionScreen 移动应用到分类等场景
 * 动画：从底部滑入(tween 450ms)，滑出(tween 400ms)，蒙层最大20%黑
 */
@Composable
fun SelectionBottomSheet(
    title: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    optionColor: Color = GoldjucXColors.primary
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val offsetY = remember { androidx.compose.animation.core.Animatable(3000f) }
    var cardHeight by remember { mutableStateOf(0f) }
    var dismissing by remember { mutableStateOf(false) }

    val animateOut: suspend () -> Unit = {
        if (!dismissing && cardHeight > 0f) {
            dismissing = true
            offsetY.animateTo(cardHeight + 200f, androidx.compose.animation.core.tween(400))
        }
    }

    LaunchedEffect(cardHeight) {
        if (cardHeight > 0f) {
            offsetY.snapTo(cardHeight + 200f)
            offsetY.animateTo(0f, androidx.compose.animation.core.tween(450))
        }
    }

    val scrimAlpha = if (cardHeight > 0f) {
        (1f - (offsetY.value / (cardHeight + 200f)).coerceIn(0f, 1f)) * 0.2f
    } else 0f

    androidx.activity.compose.BackHandler { scope.launch { animateOut(); onDismiss() } }

    Box(
        modifier = Modifier.fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { scope.launch { animateOut(); onDismiss() } },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(Modifier.fillMaxSize().background(Color(0, 0, 0, (scrimAlpha * 255).toInt().coerceIn(0, 255))))

        Column(
            modifier = Modifier
                .widthIn(max = 370.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp).padding(bottom = 28.dp)
                .onSizeChanged { cardHeight = it.height.toFloat() }
                .graphicsLayer { translationY = offsetY.value.coerceAtLeast(0f) }
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { /* 消费点击 */ }
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = TextStyle(fontSize = 18.sp, color = GoldjucXColors.onSurface, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp).padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { scope.launch { animateOut(); onSelect(option) } }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(option, style = TextStyle(fontSize = 16.sp, color = optionColor))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(50.dp)
                    .clip(RoundedCornerShape(16.dp)).background(GoldjucXColors.tertiaryContainer)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { scope.launch { animateOut(); onDismiss() } },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.settings_cancel), style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurfaceSecondary))
            }
        }
    }
}

/**
 * GoldjucX 风格滑块组件（Figma 1:1 还原）。
 *
 * ## 视觉规格
 * - 轨道：28dp 高、999dp 圆角胶囊
 * - 填充色：#3482FF（GoldjucX primary）
 * - 手柄：白色 20dp 圆形，带阴影，内边距 4dp
 * - 背景轨道：6% 黑色
 *
 * ## 交互特性
 * - 使用 Modifier.draggable（水平方向）避免与父级 verticalScroll 冲突
 * - 松手后带惯性衰减动画（frictionMultiplier = 3f）
 * - 滑动时产生连续轻微震动反馈（每 2% 变化触发一次 6ms 震动）
 * - 点击轨道任意位置可直接跳转到对应值
 *
 * ## 用途
 * 适用于需要在 0.0~1.0 范围内连续调节数值的场景，例如：
 * - 亮度补偿调节
 * - 透明度调节
 * - 音量控制
 * - 任何需要滑块交互的设置项
 *
 * @param value 当前值，范围 0.0~1.0
 * @param onValueChange 值变化回调，参数为新值（已 clamp 到 0.0~1.0）
 * @param modifier 可选的 Modifier
 */
@Composable
fun GoldjucXSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val lastVibrateValue = remember { androidx.compose.runtime.mutableFloatStateOf(value) }
    val haptic = rememberHaptic()

    var trackWidthPx by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val animatable = remember { androidx.compose.animation.core.Animatable(value) }

    LaunchedEffect(value) {
        if (!animatable.isRunning) animatable.snapTo(value)
    }

    fun applyValueWithHaptic(newValue: Float) {
        val clamped = newValue.coerceIn(0f, 1f)
        onValueChange(clamped)
        if (kotlin.math.abs(clamped - lastVibrateValue.floatValue) >= 0.02f) {
            lastVibrateValue.floatValue = clamped
            haptic(GoldjucXHaptic.GEAR_LIGHT)
        }
    }

    val draggableState = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
        if (trackWidthPx > 0f) applyValueWithHaptic(value + delta / trackWidthPx)
    }

    val flingDecay = androidx.compose.animation.core.exponentialDecay<Float>(frictionMultiplier = 3f)

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .then(Modifier.draggable(
                state = draggableState,
                orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                onDragStarted = { offset ->
                    animatable.stop()
                    if (trackWidthPx > 0f) applyValueWithHaptic(offset.x / trackWidthPx)
                },
                onDragStopped = { velocity ->
                    if (trackWidthPx > 0f) {
                        val velocityInValue = velocity / trackWidthPx * 0.5f
                        scope.launch {
                            animatable.snapTo(value)
                            animatable.animateDecay(velocityInValue, flingDecay) { applyValueWithHaptic(this.value) }
                        }
                    }
                }
            ))
    ) {
        trackWidthPx = size.width
        val trackHeight = size.height
        val cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f, trackHeight / 2f)

        // 背景轨道
        drawRoundRect(Color(0x0F000000), androidx.compose.ui.geometry.Offset.Zero,
            androidx.compose.ui.geometry.Size(size.width, trackHeight), cornerRadius)

        // 填充区域
        val handleSizePx = with(density) { 20.dp.toPx() }
        val handlePaddingPx = with(density) { 4.dp.toPx() }
        val minFillWidth = trackHeight
        val fillWidth = (minFillWidth + (size.width - minFillWidth) * value).coerceAtLeast(minFillWidth)

        drawRoundRect(GoldjucXColors.primary, androidx.compose.ui.geometry.Offset.Zero,
            androidx.compose.ui.geometry.Size(fillWidth, trackHeight), cornerRadius)

        // 白色圆形手柄（带阴影）
        val handleRadius = handleSizePx / 2f
        val handleCenterX = fillWidth - handlePaddingPx - handleRadius
        val handleCenterY = trackHeight / 2f

        drawCircle(Color(0x20000000), handleRadius + 1f, androidx.compose.ui.geometry.Offset(handleCenterX, handleCenterY + 1f))
        drawCircle(Color.White, handleRadius, androidx.compose.ui.geometry.Offset(handleCenterX, handleCenterY))
    }
}

/**
 * GoldjucX 开关联动列表收起组件。
 *
 * 将开关行和关联内容组合在一起，开关关闭时内容区域带动画收起（fadeOut + shrinkVertically），
 * 开关开启时带动画展开（fadeIn + expandVertically）。
 *
 * 用法：
 * ```kotlin
 * SwitchCollapseSection(
 *     title = "智能化启用",
 *     subtitle = "智能识别场景并为您自动启停防窥",
 *     checked = isEnabled,
 *     onCheckedChange = { setEnabled(it) }
 * ) {
 *     // 开关开启时显示的内容
 *     SettingsSectionTitle("策略列表")
 *     SettingsCard { ... }
 * }
 * ```
 */
@Composable
fun SwitchCollapseSection(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    SettingsCard {
        SettingsSwitchRow(title = title, subtitle = subtitle, checked = checked, onCheckedChange = onCheckedChange)
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = checked,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
    ) {
        Column { content() }
    }
}

/**
 * GoldjucX 类型单选弹窗（底部滑入式）
 *
 * 选中项：文字变蓝 + 右侧蓝色对勾。未选中项：黑色文字，无图标。
 * 选项无卡片背景，直接在白色弹窗上，水平内边距 28dp。
 *
 * @param title 弹窗标题
 * @param options 选项列表
 * @param selected 当前选中项（null 表示无选中）
 * @param onSelect 选中回调
 * @param onDismiss 关闭回调
 */
@Composable
fun RadioSelectionDialog(
    title: String,
    options: List<String>,
    selected: String? = null,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val offsetY = remember { androidx.compose.animation.core.Animatable(3000f) }
    var cardHeight by remember { mutableStateOf(0f) }
    var dismissing by remember { mutableStateOf(false) }

    val animateOut: suspend () -> Unit = {
        if (!dismissing && cardHeight > 0f) {
            dismissing = true
            offsetY.animateTo(cardHeight + 200f, androidx.compose.animation.core.tween(400))
        }
    }

    LaunchedEffect(cardHeight) {
        if (cardHeight > 0f) {
            offsetY.snapTo(cardHeight + 200f)
            offsetY.animateTo(0f, androidx.compose.animation.core.tween(450))
        }
    }

    val scrimAlpha = if (cardHeight > 0f) {
        (1f - (offsetY.value / (cardHeight + 200f)).coerceIn(0f, 1f)) * 0.2f
    } else 0f

    androidx.activity.compose.BackHandler { scope.launch { animateOut(); onDismiss() } }

    Box(
        modifier = Modifier.fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { scope.launch { animateOut(); onDismiss() } },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(Modifier.fillMaxSize().background(Color(0, 0, 0, (scrimAlpha * 255).toInt().coerceIn(0, 255))))

        Column(
            modifier = Modifier
                .widthIn(max = 370.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp).padding(bottom = 28.dp)
                .onSizeChanged { cardHeight = it.height.toFloat() }
                .graphicsLayer { translationY = offsetY.value.coerceAtLeast(0f) }
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { }
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                title,
                style = TextStyle(fontSize = 18.sp, color = GoldjucXColors.onSurface, textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp).padding(bottom = 16.dp)
            )

            // 选项列表
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { scope.launch { animateOut(); onSelect(option) } }
                            .defaultMinSize(minHeight = 56.dp)
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option,
                            style = TextStyle(
                                fontSize = 17.sp,
                                color = if (isSelected) GoldjucXColors.primary else GoldjucXColors.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = stringResource(R.string.settings_selected),
                                modifier = Modifier.size(20.dp),
                                tint = GoldjucXColors.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 取消按钮
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(50.dp)
                    .clip(RoundedCornerShape(16.dp)).background(GoldjucXColors.tertiaryContainer)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { scope.launch { animateOut(); onDismiss() } },
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.settings_cancel), style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurfaceSecondary))
            }
        }
    }
}

/**
 * GoldjucX 输入框组件
 *
 * 行间标题 + 圆角输入框卡片，聚焦时显示蓝色边框。
 * - 最多展示 maxVisibleLines 行文字（默认 3 行）
 * - 超出时可滚动预览
 * - 输入框背景 surfaceLow (#F3F3F3)，圆角 16dp
 * - 聚焦时 1.5dp 蓝色边框（primary），未聚焦无边框
 *
 * @param label 行间标题文字（蓝色小字）
 * @param value 当前输入值
 * @param onValueChange 输入变化回调
 * @param maxVisibleLines 最多可见行数（超出可滚动）
 * @param focusRequester 可选的 FocusRequester，用于外部控制焦点
 * @param modifier 外部修饰符
 */
@Composable
fun SettingsInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    maxVisibleLines: Int = 3,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SettingsSectionTitle(label)
        val borderMod = if (isFocused) {
            Modifier.border(1.5.dp, GoldjucXColors.primary, RoundedCornerShape(16.dp))
        } else Modifier

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface),
            maxLines = maxVisibleLines,
            cursorBrush = SolidColor(GoldjucXColors.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            placeholder,
                            style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurfaceTertiary)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .then(borderMod)
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { isFocused = it.isFocused }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

/** 滚动边界振动：滚到顶/底时触发一次 BOUNDARY 振动 */
@Composable
fun ScrollBoundaryHaptic(scrollState: androidx.compose.foundation.ScrollState, haptic: (Int) -> Unit) {
    val atTop by remember { derivedStateOf { scrollState.value == 0 } }
    val atBottom by remember { derivedStateOf { scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue } }
    var lastAtTop by remember { mutableStateOf(true) }
    var lastAtBottom by remember { mutableStateOf(false) }

    LaunchedEffect(atTop) {
        if (atTop && !lastAtTop && scrollState.maxValue > 0) haptic(GoldjucXHaptic.BOUNDARY)
        lastAtTop = atTop
    }
    LaunchedEffect(atBottom) {
        if (atBottom && !lastAtBottom && scrollState.maxValue > 0) haptic(GoldjucXHaptic.BOUNDARY)
        lastAtBottom = atBottom
    }
}

/**
 * GoldjucX 设置页脚手架。
 *
 * 内置能力：折叠标题栏、毛玻璃模糊、滚动边界振动、底部安全区留白、可选 Tab 分页。
 *
 * @param title 页面标题，null = 不显示标题栏（纯内容页）
 * @param onBack 返回按钮回调，null = 不显示返回按钮
 * @param trailingContent 标题栏右侧自定义区域（搜索按钮等），null = 右侧留空
 * @param tabs Tab 栏配置（2-5 个 GlassTab），null = 普通单页无 Tab
 * @param tabBarStyle Tab 栏毛玻璃材质，null = 使用内置默认样式
 * @param backgroundColor 页面背景色，默认浅灰
 * @param content 页面内容区，page 参数为当前 Tab 索引（无 Tab 时固定为 0）
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsPage(
    title: String? = null,                              // 页面标题，null = 不显示标题栏
    onBack: (() -> Unit)? = null,                       // 返回按钮，null = 不显示
    trailingContent: @Composable (() -> Unit)? = null,  // 标题栏右侧按钮
    tabs: List<GlassTab>? = null,                       // Tab 栏（2-5个），null = 普通单页
    tabBarStyle: dev.chrisbanes.haze.HazeStyle? = null, // Tab 栏材质，null = 内置默认
    backgroundColor: Color = GoldjucXColors.surfaceLow,
    content: @Composable ColumnScope.(page: Int) -> Unit
) {
    val hasTabs = !tabs.isNullOrEmpty()
    val showTitleBar = title != null
    val hazeState = remember { HazeState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val rememberedTabs = remember { mutableStateOf(tabs) }
    if (tabs != null) rememberedTabs.value = tabs

    val tabCount = tabs?.size ?: 0
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { tabCount.coerceAtLeast(1) })
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val selectedTab by remember { derivedStateOf { pagerState.currentPage } }

    Box(
        Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .then(if (hasTabs) Modifier.navigationBarsPadding() else Modifier)
    ) {
        if (hasTabs) {
            // ─── 带 Tab 分页模式 ───
            val scrollStates = remember(tabs!!.size) { List(tabs.size) { androidx.compose.foundation.ScrollState(0) } }
            val currentScrollState = scrollStates[selectedTab]
            val haptic = rememberHaptic()

            ScrollBoundaryHaptic(currentScrollState, haptic)

            val titleBarHeight = if (showTitleBar) {
                CollapsibleTitleBar(
                    title = title!!, scrollState = currentScrollState,
                    onBack = onBack,
                    showBackButton = onBack != null,
                    trailingContent = trailingContent,
                    backgroundColor = backgroundColor,
                    hazeState = hazeState
                )
            } else 0.dp

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) { page ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollStates[page])
                        .padding(top = titleBarHeight)
                ) {
                    content(page)
                    Spacer(Modifier.height(100.dp))
                }
            }

        } else {
            // ─── 普通单页模式 ───
            val scrollState = rememberScrollState()
            val haptic = rememberHaptic()

            ScrollBoundaryHaptic(scrollState, haptic)

            val titleBarHeight = if (showTitleBar) {
                CollapsibleTitleBar(
                    title = title!!, scrollState = scrollState,
                    onBack = onBack,
                    showBackButton = onBack != null,
                    trailingContent = trailingContent,
                    backgroundColor = backgroundColor,
                    hazeState = hazeState
                )
            } else 0.dp

            Column(
                Modifier
                    .fillMaxSize()
                    .then(if (showTitleBar) Modifier.hazeSource(state = hazeState) else Modifier)
                    .verticalScroll(scrollState)
                    .padding(top = titleBarHeight)
            ) {
                content(0)
                Spacer(Modifier.height(100.dp))
            }
        }

        // ─── Tab 栏（带入场/退场动画） ───
        AnimatedVisibility(
            visible = hasTabs,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val exitTabs = rememberedTabs.value
            if (exitTabs != null) {
                GlassBottomBar(
                    tabs = exitTabs,
                    selectedTab = selectedTab,
                    onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                    hazeState = hazeState,
                    hazeStyle = tabBarStyle
                )
            }
        }
    }
}


/** 滑块设置区段：标题 + 卡片（描述 + 滑块） */
@Composable
fun SliderSettingSection(
    sectionTitle: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    SettingsSectionTitle(sectionTitle)
    SettingsCard {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(description, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
            Spacer(Modifier.height(12.dp))
            GoldjucXSlider(value = value, onValueChange = onValueChange)
        }
    }
}

/** 滑块设置行：主标题 + 可选重置按钮 + 副标题描述 + 滑块 */
@Composable
fun SliderSettingRow(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onReset: (() -> Unit)? = null,
    resetText: String = "重置"
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
            if (onReset != null) {
                Text(
                    resetText,
                    style = TextStyle(fontSize = 13.sp, color = GoldjucXColors.primary),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onReset() }
                )
            }
        }
        Text(subtitle, style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))
        Spacer(Modifier.height(8.dp))
        GoldjucXSlider(value = value, onValueChange = onValueChange)
    }
}
