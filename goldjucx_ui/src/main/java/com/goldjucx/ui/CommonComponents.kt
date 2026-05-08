package com.goldjucx.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.goldjucx.ui.R
import kotlinx.coroutines.launch

enum class ButtonType { Primary, Normal, Danger }

fun Modifier.bounceClick() = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                scope.launch { scale.animateTo(0.9f, tween(80)) }
                waitForUpOrCancellation()
                scope.launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f)) }
            }
        }
}

/**
 * 统一的 GoldjucX 风格开关：49x28dp 轨道，20x20dp 白色圆形滑块
 * 开启：蓝色轨道 #3482FF   关闭：10% 黑色轨道
 */
@Composable
fun GoldjucXSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val haptic = rememberHaptic()
    val thumbOffset = animateFloatAsState(
        targetValue = if (checked) 21f else 0f,
        animationSpec = tween(200), label = "thumb"
    )
    val trackColor = animateColorAsState(
        targetValue = if (checked) GoldjucXColors.primary else Color(0x1A000000),
        animationSpec = tween(200), label = "track"
    )
    Box(
        modifier = Modifier
            .width(49.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic(GoldjucXHaptic.SWITCH)
                    onCheckedChange(!checked)
                }
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.value.dp)
                .size(20.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    CircleIconButton(iconRes = R.drawable.ic_back, contentDescription = stringResource(R.string.common_back), onClick = onClick)
}

/**
 * GoldjucX 统一圆形图标按钮
 *
 * 44dp 圆形，8dp 阴影，95% 白色背景（#F2FFFFFF），21dp 图标。
 * 所有页面的圆形按钮（返回、关闭、搜索、确认等）统一使用此组件。
 *
 * @param iconRes 图标资源 ID
 * @param contentDescription 无障碍描述
 * @param onClick 点击回调
 * @param tint 图标着色，默认 GoldjucXColors.onSurface（黑色）
 * @param enabled 是否可点击，禁用时图标变灰
 */
@Composable
fun CircleIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = GoldjucXColors.onSurface,
    enabled: Boolean = true
) {
    val haptic = rememberHaptic()
    Box(
        modifier = modifier
            .bounceClick()
            .size(44.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = GoldjucXColors.shadowAmbientLight,
                spotColor = GoldjucXColors.shadowSpotLight
            )
            .clip(CircleShape)
            .background(GoldjucXColors.buttonBackground)
            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { haptic(GoldjucXHaptic.BUTTON_SMALL); onClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(21.dp),
            tint = if (enabled) tint else GoldjucXColors.onSurfaceQuaternary
        )
    }
}

@Composable
fun ListItem(
    title: String,
    subtitle: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = GoldjucXColors.onSurface)
            if (subtitle != null) {
                Text(subtitle, fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary)
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else if (onClick != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(10.dp, 16.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = GoldjucXColors.gray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
    )
}

@Composable
fun PermissionIcon(@DrawableRes iconRes: Int, color: Color = GoldjucXColors.primary) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White
        )
    }
}

/** GoldjucX 主操作按钮：蓝底白字，16dp 圆角，bounceClick
 *  compact=false（默认）：336×50dp，用于页面底部独立按钮
 *  compact=true：自适应宽度×40dp，用于卡片内部紧凑按钮
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    val haptic = rememberHaptic()
    Button(
        onClick = { haptic(GoldjucXHaptic.BUTTON_LARGE); onClick() },
        modifier = modifier.bounceClick().then(
            if (compact) Modifier.height(40.dp)
            else Modifier.width(336.dp).height(50.dp)
        ),
        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
        enabled = enabled,
        contentPadding = if (compact) PaddingValues(horizontal = 20.dp, vertical = 0.dp)
            else ButtonDefaults.ContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldjucXColors.primary,
            contentColor = Color.White
        )
    ) {
        Text(text, style = TextStyle(fontSize = if (compact) 15.sp else 17.sp, fontWeight = FontWeight(380)))
    }
}

/** GoldjucX 次级按钮：灰底，fillMaxWidth×50dp，16dp 圆角，bounceClick */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = GoldjucXColors.onSurfaceSecondary
) {
    Box(
        modifier = modifier
            .bounceClick()
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GoldjucXColors.tertiaryContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = TextStyle(fontSize = 17.sp, color = textColor))
    }
}

/** GoldjucX 分段选择器，glassMode=true 时选中态用液态玻璃 */
@Composable
fun SegmentedTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassMode: Boolean = false,
    glassMaterial: LiquidGlassMaterial? = null
) {
    val haptic = rememberHaptic()
    val effectiveMaterial = glassMaterial ?: LiquidGlassDefaults.material

    if (!glassMode) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GoldjucXColors.tertiaryContainer)
                .padding(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedIndex == index) Color.White else Color.Transparent)
                        .clickable { haptic(GoldjucXHaptic.GEAR_HEAVY); onTabSelected(index) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title, fontSize = 14.sp,
                        color = if (selectedIndex == index) GoldjucXColors.onSurface else GoldjucXColors.onSurfaceTertiary
                    )
                }
            }
        }
    } else {
        val tabCount = tabs.size
        val animatedIndex = animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
            label = "glassTab"
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Shader 层
            LiquidGlassScene(
                material = effectiveMaterial,
                modifier = Modifier.fillMaxSize()
            ) {
                // 背景
                Box(Modifier.fillMaxSize().background(GoldjucXColors.tertiaryContainer))
                // 玻璃滑块
                LiquidGlass(
                    cornerRadius = 10.dp,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(1f / tabCount)
                        .graphicsLayer {
                            translationX = animatedIndex.value * size.width
                        }
                ) {}
            }
            // 文字覆盖层
            Row(Modifier.fillMaxSize().padding(4.dp)) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { haptic(GoldjucXHaptic.GEAR_HEAVY); onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title, fontSize = 14.sp,
                            color = if (selectedIndex == index) GoldjucXColors.onSurface else GoldjucXColors.onSurfaceTertiary
                        )
                    }
                }
            }
        }
    }
}

/** 等宽代码块：F5F5F5 背景，12dp 圆角，可横向滚动 */
@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoldjucXColors.codeBackground)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            code,
            style = TextStyle(
                fontSize = 12.sp,
                color = GoldjucXColors.onSurface,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        )
    }
}

/** 计算连续卡片列表中第 index 项的圆角形状 */
fun cardShape(index: Int, total: Int, cornerRadius: Dp = 20.dp): RoundedCornerShape {
    return when {
        total == 1 -> RoundedCornerShape(cornerRadius)
        index == 0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        index == total - 1 -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
        else -> RoundedCornerShape(0.dp)
    }
}

/** 应用图标 + 名称 + 开关 行 */
@Composable
fun AppSwitchRow(
    icon: ImageBitmap,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            bitmap = icon, contentDescription = label,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
        )
        Text(
            label, style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface),
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
        )
        GoldjucXSwitch(checked) { onCheckedChange(it) }
    }
}

/** 可选应用图标（网格），带勾选徽章 */
@Composable
fun SelectableAppIcon(
    icon: ImageBitmap,
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 60.dp
) {
    val haptic = rememberHaptic()
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { haptic(GoldjucXHaptic.BUTTON_SMALL); onToggle() }
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Image(
                bitmap = icon, contentDescription = label,
                modifier = Modifier.size(iconSize).clip(RoundedCornerShape(16.dp))
            )
            Icon(
                painter = painterResource(
                    if (isSelected) R.drawable.ic_check_circle else R.drawable.ic_uncheck_circle
                ),
                contentDescription = null,
                modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).offset(x = 3.dp, y = 3.dp),
                tint = Color.Unspecified
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            label,
            style = TextStyle(fontSize = 14.sp, lineHeight = 14.sp, color = GoldjucXColors.onSurfaceTertiary, textAlign = TextAlign.Center),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

/** 功能介绍列表项：图标 + 标题 + 副标题 */
@Composable
fun FeatureListItem(@DrawableRes iconRes: Int, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(painterResource(id = iconRes), title, Modifier.size(36.dp), tint = Color.Unspecified)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight(380), lineHeight = 16.sp, color = GoldjucXColors.onSurface))
            Text(subtitle, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight(330), lineHeight = 13.sp, color = GoldjucXColors.onSurfaceQuaternary))
        }
    }
}
