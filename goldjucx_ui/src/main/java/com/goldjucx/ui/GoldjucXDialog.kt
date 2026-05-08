package com.goldjucx.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ════════════════════════════════════════════════════════════════════
// 尺寸规范
// ════════════════════════════════════════════════════════════════════

const val DIALOG_MAX_HEIGHT_FRACTION: Float = 0.85f
val DIALOG_MIN_HEIGHT: Dp = 160.dp
val DIALOG_MAX_WIDTH: Dp = 370.dp
val DIALOG_CORNER_RADIUS: Dp = 36.dp
val DIALOG_BUTTON_HEIGHT: Dp = 50.dp
val DIALOG_BUTTON_RADIUS: Dp = 16.dp

// ════════════════════════════════════════════════════════════════════
// 背景模型
// ════════════════════════════════════════════════════════════════════

enum class DialogBackground { White, Gray }

// ════════════════════════════════════════════════════════════════════
// 按钮模型
// ════════════════════════════════════════════════════════════════════

enum class DialogButtonLayout { Horizontal, Vertical }

data class DialogButton(
    val id: String,
    val text: String,
    val type: ButtonType = ButtonType.Normal,
    val enabled: Boolean = true,
)

// ════════════════════════════════════════════════════════════════════
// 内容模型
// ════════════════════════════════════════════════════════════════════

data class DialogInput(
    val value: String,
    val onValueChange: (String) -> Unit,
    val placeholder: String = "",
    val label: String? = null,
    val maxLines: Int = 3,
    val autoFocus: Boolean = true,
)

sealed class DialogSelection {
    data class Single(
        val options: List<String>,
        val selected: String?,
        val onSelect: (String) -> Unit,
    ) : DialogSelection()

    data class Multi(
        val options: List<String>,
        val selected: Set<String>,
        val onToggle: (String) -> Unit,
    ) : DialogSelection()
}

// ════════════════════════════════════════════════════════════════════
// 主 API
// ════════════════════════════════════════════════════════════════════

@Composable
fun GoldjucXDialog(
    title: String,
    buttons: List<DialogButton>,
    onButtonClick: (id: String) -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
    caption: String? = null,
    @DrawableRes imageRes: Int? = null,
    input: DialogInput? = null,
    selection: DialogSelection? = null,
    background: DialogBackground = DialogBackground.White,
    buttonLayout: DialogButtonLayout = defaultLayoutFor(buttons.size),
) {
    require(buttons.isNotEmpty()) { "GoldjucXDialog 至少需要 1 个按钮" }
    require(buttons.size <= 4) { "GoldjucXDialog 最多支持 4 个按钮，当前传入 ${buttons.size}" }
    require(buttonLayout != DialogButtonLayout.Horizontal || buttons.size == 2) {
        "横向排布（Horizontal）仅支持 2 个按钮，当前传入 ${buttons.size}"
    }

    DialogShell(onDismiss = onDismiss, background = background) { animateOut ->
        val scope = rememberCoroutineScope()

        Text(
            text = title,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(500), color = GoldjucXColors.onSurface),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp)
        )

        if (imageRes != null) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        if (!message.isNullOrEmpty()) {
            Text(
                text = message,
                style = TextStyle(fontSize = 16.sp, color = GoldjucXColors.onSurfaceSecondary, lineHeight = 22.sp),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)
            )
        }

        if (!caption.isNullOrEmpty()) {
            Text(
                text = caption,
                style = TextStyle(fontSize = 13.sp, color = GoldjucXColors.onSurfaceQuaternary, lineHeight = 18.sp),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)
            )
        }

        if (input != null) {
            DialogInputField(input)
        }

        when (selection) {
            is DialogSelection.Single -> DialogSingleSelectionList(selection)
            is DialogSelection.Multi -> DialogMultiSelectionList(selection)
            null -> Unit
        }

        val onClick: (DialogButton) -> Unit = { btn ->
            if (btn.enabled) {
                scope.launch {
                    animateOut()
                    onButtonClick(btn.id)
                }
            }
        }
        when (buttonLayout) {
            DialogButtonLayout.Horizontal -> HorizontalButtons(buttons, onClick)
            DialogButtonLayout.Vertical -> VerticalButtons(buttons, onClick)
        }
    }
}

private fun defaultLayoutFor(count: Int): DialogButtonLayout =
    if (count == 2) DialogButtonLayout.Horizontal else DialogButtonLayout.Vertical

// ════════════════════════════════════════════════════════════════════
// 兼容旧 API：简化版（title + message + confirm/dismiss）
// ════════════════════════════════════════════════════════════════════

@Composable
fun GoldjucXDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.dialog_default_confirm),
    dismissText: String? = stringResource(R.string.dialog_default_dismiss),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val buttons = if (dismissText != null) {
        listOf(
            DialogButton("dismiss", dismissText, ButtonType.Normal),
            DialogButton("confirm", confirmText, ButtonType.Primary),
        )
    } else {
        listOf(DialogButton("confirm", confirmText, ButtonType.Primary))
    }
    GoldjucXDialog(
        title = title,
        message = message,
        buttons = buttons,
        onButtonClick = { id ->
            when (id) {
                "confirm" -> onConfirm()
                "dismiss" -> onDismiss()
            }
        },
        onDismiss = onDismiss,
    )
}

// ════════════════════════════════════════════════════════════════════
// 兼容旧 API：自定义 content 版
// ════════════════════════════════════════════════════════════════════

@Composable
fun GoldjucXDialog(
    title: String,
    content: @Composable () -> Unit,
    confirmText: String = stringResource(R.string.dialog_default_confirm),
    dismissText: String = stringResource(R.string.dialog_default_dismiss),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogShell(onDismiss = onDismiss) { animateOut ->
        val scope = rememberCoroutineScope()

        Text(
            text = title,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(500), color = GoldjucXColors.onSurface),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp)
        )

        Box(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) { content() }

        HorizontalButtons(
            buttons = listOf(
                DialogButton("dismiss", dismissText, ButtonType.Normal),
                DialogButton("confirm", confirmText, ButtonType.Primary),
            ),
            onClick = { btn ->
                scope.launch {
                    animateOut()
                    when (btn.id) {
                        "confirm" -> onConfirm()
                        "dismiss" -> onDismiss()
                    }
                }
            }
        )
    }
}

// ════════════════════════════════════════════════════════════════════
// 兼容旧 API：多按钮 + imageRes 版（isPrimary 风格）
// ════════════════════════════════════════════════════════════════════

data class LegacyDialogButton(
    val text: String,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun GoldjucXDialog(
    title: String,
    message: String,
    @DrawableRes imageRes: Int? = null,
    buttons: List<LegacyDialogButton>,
    onDismiss: () -> Unit
) {
    val newButtons = buttons.mapIndexed { i, btn ->
        DialogButton(
            id = "btn_$i",
            text = btn.text,
            type = if (btn.isPrimary) ButtonType.Primary else ButtonType.Normal
        )
    }
    GoldjucXDialog(
        title = title,
        message = message,
        imageRes = imageRes,
        buttons = newButtons,
        buttonLayout = if (newButtons.size == 2) DialogButtonLayout.Horizontal else DialogButtonLayout.Vertical,
        onButtonClick = { id ->
            val idx = id.removePrefix("btn_").toIntOrNull() ?: return@GoldjucXDialog
            buttons.getOrNull(idx)?.onClick?.invoke()
        },
        onDismiss = onDismiss
    )
}

// ════════════════════════════════════════════════════════════════════
// 内部：输入框 + 选择列表
// ════════════════════════════════════════════════════════════════════

@Composable
private fun DialogInputField(input: DialogInput) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var focused by remember { mutableStateOf(false) }

    if (input.autoFocus) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (input.label != null) {
            Text(
                text = input.label,
                style = TextStyle(fontSize = 13.sp, color = GoldjucXColors.onSurfaceTertiary)
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GoldjucXColors.surfaceLow)
                .then(
                    if (focused) Modifier.border(1.5.dp, GoldjucXColors.primary, RoundedCornerShape(14.dp))
                    else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            if (input.value.isEmpty() && input.placeholder.isNotEmpty()) {
                Text(
                    text = input.placeholder,
                    style = TextStyle(fontSize = 16.sp, color = GoldjucXColors.onSurface.copy(alpha = 0.25f))
                )
            }
            BasicTextField(
                value = input.value,
                onValueChange = input.onValueChange,
                textStyle = TextStyle(fontSize = 16.sp, color = GoldjucXColors.onSurface),
                maxLines = input.maxLines,
                cursorBrush = SolidColor(GoldjucXColors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

@Composable
private fun DialogSingleSelectionList(selection: DialogSelection.Single) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        selection.options.forEach { option ->
            val isSelected = option == selection.selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { selection.onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option,
                    style = TextStyle(
                        fontSize = 17.sp,
                        color = if (isSelected) GoldjucXColors.primary else GoldjucXColors.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GoldjucXColors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogMultiSelectionList(selection: DialogSelection.Multi) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        selection.options.forEach { option ->
            val isChecked = option in selection.selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { selection.onToggle(option) }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isChecked) R.drawable.ic_check_circle else R.drawable.ic_uncheck_circle
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.Unspecified
                )
                Text(
                    text = option,
                    style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 内部：外壳 + 按钮布局
// ════════════════════════════════════════════════════════════════════

@Composable
private fun DialogShell(
    onDismiss: () -> Unit,
    background: DialogBackground = DialogBackground.White,
    content: @Composable ColumnScope.(animateOut: suspend () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val maxDialogHeight = (screenHeightDp * DIALOG_MAX_HEIGHT_FRACTION).dp
    val density = LocalDensity.current
    val offscreenPx = with(density) { (screenHeightDp * 1.5).dp.toPx() }

    val offsetY = remember { Animatable(offscreenPx) }
    var dismissing by remember { mutableStateOf(false) }

    val animateOut: suspend () -> Unit = {
        if (!dismissing) {
            dismissing = true
            offsetY.animateTo(offscreenPx, tween(350))
        }
    }

    LaunchedEffect(Unit) {
        offsetY.animateTo(0f, tween(400))
    }

    val scrimAlpha = (1f - (offsetY.value / offscreenPx).coerceIn(0f, 1f)) * 0.2f

    BackHandler { scope.launch { animateOut(); onDismiss() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { scope.launch { animateOut(); onDismiss() } },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0, 0, 0, (scrimAlpha * 255).toInt().coerceIn(0, 255)))
        )

        Box(
            modifier = Modifier
                .widthIn(max = DIALOG_MAX_WIDTH)
                .fillMaxWidth()
                .heightIn(min = DIALOG_MIN_HEIGHT, max = maxDialogHeight)
                .imePadding()
                .padding(horizontal = 12.dp)
                .padding(bottom = 28.dp)
                .graphicsLayer { translationY = offsetY.value.coerceAtLeast(0f) }
                .clip(RoundedCornerShape(DIALOG_CORNER_RADIUS))
                .background(
                    when (background) {
                        DialogBackground.White -> Color.White
                        DialogBackground.Gray -> GoldjucXColors.surfaceLow
                    }
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content(animateOut)
            }
        }
    }
}

@Composable
private fun HorizontalButtons(
    buttons: List<DialogButton>,
    onClick: (DialogButton) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { btn ->
            DialogButtonBox(button = btn, modifier = Modifier.weight(1f), onClick = onClick)
        }
    }
}

@Composable
private fun VerticalButtons(
    buttons: List<DialogButton>,
    onClick: (DialogButton) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { btn ->
            DialogButtonBox(button = btn, modifier = Modifier.fillMaxWidth(), onClick = onClick)
        }
    }
}

@Composable
private fun DialogButtonBox(
    button: DialogButton,
    modifier: Modifier,
    onClick: (DialogButton) -> Unit
) {
    val (bgColor, textColor) = when (button.type) {
        ButtonType.Primary -> GoldjucXColors.primary to Color.White
        ButtonType.Normal -> GoldjucXColors.tertiaryContainer to GoldjucXColors.onSurfaceSecondary
        ButtonType.Danger -> GoldjucXColors.tertiaryContainer to GoldjucXColors.danger
    }
    val alpha = if (button.enabled) 1f else 0.4f

    Box(
        modifier = modifier
            .then(if (button.enabled) Modifier.bounceClick() else Modifier)
            .height(DIALOG_BUTTON_HEIGHT)
            .clip(RoundedCornerShape(DIALOG_BUTTON_RADIUS))
            .background(bgColor.copy(alpha = bgColor.alpha * alpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = button.enabled,
                onClick = { onClick(button) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = button.text,
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = if (button.type == ButtonType.Primary) FontWeight(500) else FontWeight.Normal,
                color = textColor.copy(alpha = textColor.alpha * alpha)
            )
        )
    }
}
