package com.goldjucx.showcase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.goldjucx.ui.*
import com.goldjucx.showcase.R
import com.goldjucx.showcase.ui.theme.OnSurfaceQuaternary
import kotlinx.coroutines.launch

@Composable
fun UIShowcaseScreen(onBack: (() -> Unit)? = null, onNavigateToDemo: (String) -> Unit) {
    data class DemoItem(val title: String, val subtitle: String, val route: String)

    val pageTemplates = listOf(
        DemoItem(stringResource(R.string.ui_showcase_item_page_config), "SettingsPage", "demo_settings_page_config"),
        DemoItem(stringResource(R.string.ui_showcase_item_page_controls), "SettingsLayout", "demo_settings_layout"),
    )

    val overlays = listOf(
        DemoItem(stringResource(R.string.ui_showcase_item_bottom_sheet), "BottomSheetDrawer", "demo_bottom_sheet_drawer"),
        DemoItem(stringResource(R.string.ui_showcase_item_dialog), "GoldjucXDialog", "demo_dialog"),
    )

    val controls = listOf(
        DemoItem(stringResource(R.string.ui_showcase_item_common_components), "CommonComponents", "demo_common_components"),
    )

    val materials = listOf(
        DemoItem(stringResource(R.string.ui_showcase_item_material_preview), "Material Preview", "demo_material_preview"),
    )

    SettingsPage(
        title = stringResource(R.string.ui_showcase_title),
        onBack = onBack
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.illustration_ui_showcase),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                .clip(RoundedCornerShape(20.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
        )

        SettingsSectionTitle(stringResource(R.string.ui_showcase_section_page_templates))
        SettingsCard(items = pageTemplates) { item ->
            SettingsNavigateRow(
                title = item.title,
                value = item.subtitle,
                onClick = { onNavigateToDemo(item.route) }
            )
        }

        SettingsSectionTitle(stringResource(R.string.ui_showcase_section_overlays))
        SettingsCard(items = overlays) { item ->
            SettingsNavigateRow(
                title = item.title,
                value = item.subtitle,
                onClick = { onNavigateToDemo(item.route) }
            )
        }

        SettingsSectionTitle(stringResource(R.string.ui_showcase_section_controls))
        SettingsCard(items = controls) { item ->
            SettingsNavigateRow(
                title = item.title,
                value = item.subtitle,
                onClick = { onNavigateToDemo(item.route) }
            )
        }

        SettingsSectionTitle(stringResource(R.string.ui_showcase_section_materials))
        SettingsCard(items = materials) { item ->
            SettingsNavigateRow(
                title = item.title,
                value = item.subtitle,
                onClick = { onNavigateToDemo(item.route) }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Demo: 页面配置可视化器
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun DemoSettingsPageConfig(onBack: () -> Unit) {
    val bgColorLabels = listOf(
        stringResource(R.string.demo_page_config_color_default),
        stringResource(R.string.demo_page_config_color_white),
        stringResource(R.string.demo_page_config_color_light_blue),
        stringResource(R.string.demo_page_config_color_light_gray)
    )
    val bgColorValues = listOf(GoldjucXColors.surfaceLow, Color.White, Color(0xFFE3F2FD), Color(0xFFEEEEEE))
    val bgColorOptions = bgColorLabels.zip(bgColorValues)
    var showTitle by remember { mutableStateOf(true) }
    var showBack by remember { mutableStateOf(true) }
    var trailingCount by remember { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var enableTabs by remember { mutableStateOf(false) }
    var tabLiquidGlass by remember { mutableStateOf(false) }
    var tabsActive by remember { mutableStateOf(false) }
    var tabSlider by remember { mutableFloatStateOf(1f / 3f) }
    var bgColorIndex by remember { mutableIntStateOf(0) }
    var showBgMenu by remember { mutableStateOf(false) }
    var showTrailingMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(enableTabs) {
        kotlinx.coroutines.delay(350)
        tabsActive = enableTabs
    }

    val tabCount = 2 + (tabSlider * 3f + 0.5f).toInt().coerceIn(0, 3)
    val allTabs = listOf(
        GlassTab(stringResource(R.string.demo_page_config_tab_general), R.drawable.ic_settings),
        GlassTab(stringResource(R.string.demo_page_config_tab_privacy), R.drawable.ic_person),
        GlassTab(stringResource(R.string.demo_page_config_tab_about), R.drawable.ic_star),
        GlassTab(stringResource(R.string.demo_page_config_tab_home), R.drawable.ic_home),
        GlassTab(stringResource(R.string.demo_page_config_tab_favorite), R.drawable.ic_star),
    )

    if (isSearching) {
        // ─── 搜索页（全屏态） ───
        val demoItems = listOf("标题栏", "返回按钮", "右侧按钮数量", "Tab 栏", "背景颜色", "设置项 1", "设置项 2", "设置项 3")
        val filteredItems = remember(searchQuery) {
            if (searchQuery.isBlank()) emptyList()
            else demoItems.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(GoldjucXColors.surfaceLow)
                .statusBarsPadding()
        ) {
            // 搜索栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f).height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x0D000000))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.ic_search), "搜索", Modifier.size(18.dp), tint = OnSurfaceQuaternary)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("搜索设置项…", style = TextStyle(fontSize = 17.sp, color = OnSurfaceQuaternary))
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface),
                            cursorBrush = SolidColor(GoldjucXColors.primary),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                CircleIconButton(
                    iconRes = R.drawable.ic_close,
                    contentDescription = "关闭",
                    onClick = { isSearching = false; searchQuery = "" }
                )
            }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            // 搜索结果区
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            ) {
                if (searchQuery.isBlank()) {
                    Box(Modifier.fillMaxWidth().padding(top = 120.dp), contentAlignment = Alignment.Center) {
                        Text("输入关键词搜索设置项", style = TextStyle(fontSize = 14.sp, color = OnSurfaceQuaternary))
                    }
                } else if (filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 120.dp), contentAlignment = Alignment.Center) {
                        Text("无匹配结果", style = TextStyle(fontSize = 14.sp, color = OnSurfaceQuaternary))
                    }
                } else {
                    SettingsSectionTitle("找到 ${filteredItems.size} 项")
                    SettingsCard {
                        filteredItems.forEach { item ->
                            SettingsNavigateRow(title = item, onClick = {})
                        }
                    }
                }
            }
        }
    } else {
        // ─── 正常页面配置态 ───
        SettingsPage(
            title = if (showTitle) stringResource(R.string.demo_page_config_title) else null,
            onBack = if (showBack) onBack else null,
            trailingContent = if (trailingCount > 0) ({
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CircleIconButton(
                        iconRes = R.drawable.ic_search,
                        contentDescription = "搜索",
                        onClick = { isSearching = true }
                    )
                    if (trailingCount >= 2) {
                        AnchoredPopupMenuBox(
                            options = listOf("分享", "收藏", "举报"),
                            selected = "",
                            expanded = showMoreMenu,
                            onSelect = { showMoreMenu = false },
                            onDismiss = { showMoreMenu = false }
                        ) {
                            CircleIconButton(
                                iconRes = R.drawable.ic_more,
                                contentDescription = "更多",
                                onClick = { showMoreMenu = true }
                            )
                        }
                    }
                    if (trailingCount >= 3) {
                        CircleIconButton(
                            iconRes = com.goldjucx.ui.R.drawable.ic_check,
                            contentDescription = "确认",
                            onClick = {}
                        )
                    }
                }
            }) else null,
            tabs = if (tabsActive) allTabs.take(tabCount) else null,
            tabBarLiquidGlass = tabLiquidGlass,
            backgroundColor = bgColorOptions[bgColorIndex].second
        ) { page ->
            SettingsSectionTitle(stringResource(R.string.demo_page_config_section_config))
            SettingsCard {
                SettingsSwitchRow(
                    title = stringResource(R.string.demo_page_config_switch_title_bar),
                    subtitle = "title",
                    checked = showTitle,
                    onCheckedChange = { showTitle = it }
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.demo_page_config_switch_back_button),
                    subtitle = "onBack",
                    checked = showBack,
                    onCheckedChange = { showBack = it }
                )
                AnchoredPopupMenuBox(
                    options = listOf("0", "1", "2", "3"),
                    selected = "$trailingCount",
                    expanded = showTrailingMenu,
                    onSelect = { trailingCount = it.toInt(); showTrailingMenu = false },
                    onDismiss = { showTrailingMenu = false }
                ) {
                    SettingsValueRow(
                        title = stringResource(R.string.demo_page_config_trailing_count),
                        subtitle = "trailingContent",
                        value = stringResource(R.string.demo_page_config_trailing_value, trailingCount),
                        onClick = { showTrailingMenu = true }
                    )
                }
            }

            SwitchCollapseSection(
                title = stringResource(R.string.demo_page_config_tab_bar),
                subtitle = "tabs",
                checked = enableTabs,
                onCheckedChange = { enableTabs = it }
            ) {
                SliderSettingSection(
                    sectionTitle = stringResource(R.string.demo_page_config_tab_count, tabCount),
                    description = stringResource(R.string.demo_page_config_tab_count_desc),
                    value = tabSlider,
                    onValueChange = { tabSlider = it }
                )
                SettingsCard {
                    SettingsSwitchRow(
                        title = "启用液态玻璃",
                        subtitle = "liquidGlass",
                        checked = tabLiquidGlass,
                        onCheckedChange = { tabLiquidGlass = it }
                    )
                }
            }

            SettingsCard {
                AnchoredPopupMenuBox(
                    options = bgColorOptions.map { it.first },
                    selected = bgColorOptions[bgColorIndex].first,
                    expanded = showBgMenu,
                    onSelect = { name -> bgColorIndex = bgColorOptions.indexOfFirst { it.first == name }; showBgMenu = false },
                    onDismiss = { showBgMenu = false }
                ) {
                    SettingsValueRow(
                        title = stringResource(R.string.demo_page_config_bg_color),
                        subtitle = "backgroundColor",
                        value = bgColorOptions[bgColorIndex].first,
                        onClick = { showBgMenu = true }
                    )
                }
            }

            SettingsSectionTitle(stringResource(R.string.demo_page_config_content_tab, page))
            repeat(10) { i ->
                SettingsCard {
                    SettingsTextRow(
                        title = stringResource(R.string.demo_page_config_item_n, i + 1),
                        subtitle = stringResource(R.string.demo_page_config_item_subtitle)
                    )
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Demo: 页面控件
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

private enum class RowStyle(val labelRes: Int) {
    Switch(R.string.demo_layout_style_switch),
    Navigate(R.string.demo_layout_style_navigate),
    Value(R.string.demo_layout_style_value),
    Text(R.string.demo_layout_style_text),
    IconSwitch(R.string.demo_layout_style_icon_switch),
    IconNavigate(R.string.demo_layout_style_icon_navigate),
    Action(R.string.demo_layout_style_action),
    Slider(R.string.demo_layout_style_slider),
    InputField(R.string.demo_layout_style_input),
    Collapse(R.string.demo_layout_style_collapse),
    SwitchCollapse(R.string.demo_layout_style_switch_collapse)
}

@Composable
fun DemoSettingsLayout(onBack: () -> Unit) {
    val maxRows = 5
    var cardCount by remember { mutableIntStateOf(2) }
    var showCountPicker by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(true) }

    // 固定 5 槽位，避免越界
    val rowStyles = remember { mutableStateListOf(RowStyle.Switch, RowStyle.Navigate, RowStyle.Value, RowStyle.Text, RowStyle.IconSwitch) }
    val rowPickerExpanded = remember { mutableStateListOf(false, false, false, false, false) }
    val switchStates = remember { mutableStateListOf(true, false, true, false, true) }
    val collapseStates = remember { mutableStateListOf(false, false, false, false, false) }
    val defaultAutoText = stringResource(R.string.demo_layout_value_auto)
    val defaultManualText = stringResource(R.string.demo_layout_value_manual)
    val defaultSampleText = stringResource(R.string.demo_layout_sample_text)
    val valueSelections = remember { mutableStateListOf(defaultAutoText, defaultManualText, defaultAutoText, defaultManualText, defaultAutoText) }
    val valuePopups = remember { mutableStateListOf(false, false, false, false, false) }
    var sliderValue by remember { mutableFloatStateOf(0.6f) }
    val inputValues = remember { mutableStateListOf(defaultSampleText, defaultSampleText, defaultSampleText, defaultSampleText, defaultSampleText) }
    var inputShowPlaceholder by remember { mutableStateOf(false) }

    SettingsPage(
        title = stringResource(R.string.demo_layout_title),
        onBack = onBack
    ) {
        // ═══ 配置区 ═══
        SettingsSectionTitle(stringResource(R.string.demo_layout_section_config))
        SettingsCard {
            AnchoredPopupMenuBox(
                options = listOf("1", "2", "3", "4", "5"),
                selected = cardCount.toString(),
                expanded = showCountPicker,
                onSelect = { cardCount = it.toIntOrNull() ?: 2; showCountPicker = false },
                onDismiss = { showCountPicker = false }
            ) {
                SettingsValueRow(title = stringResource(R.string.demo_layout_card_count), value = stringResource(R.string.demo_layout_card_count_value, cardCount), onClick = { showCountPicker = true })
            }
            SettingsSwitchRow(title = stringResource(R.string.demo_layout_show_subtitle), subtitle = stringResource(R.string.demo_layout_show_subtitle_desc), checked = showSubtitle, onCheckedChange = { showSubtitle = it })
            if (rowStyles.take(cardCount.coerceAtMost(maxRows)).contains(RowStyle.InputField)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.demo_layout_placeholder_mode),
                    subtitle = stringResource(R.string.demo_layout_placeholder_mode_desc),
                    checked = inputShowPlaceholder,
                    onCheckedChange = {
                        inputShowPlaceholder = it
                        if (it) {
                            for (j in 0 until maxRows) inputValues[j] = ""
                        } else {
                            for (j in 0 until maxRows) inputValues[j] = defaultSampleText
                        }
                    }
                )
            }
        }

        // 每行样式选择
        val rowStyleLabels = RowStyle.values().map { stringResource(it.labelRes) }
        SettingsSectionTitle(stringResource(R.string.demo_layout_section_row_styles))
        SettingsCard {
            for (i in 0 until cardCount.coerceAtMost(maxRows)) {
                AnchoredPopupMenuBox(
                    options = rowStyleLabels,
                    selected = stringResource(rowStyles[i].labelRes),
                    expanded = rowPickerExpanded[i],
                    onSelect = { label ->
                        val idx = rowStyleLabels.indexOf(label)
                        if (idx >= 0) rowStyles[i] = RowStyle.values()[idx]
                        rowPickerExpanded[i] = false
                    },
                    onDismiss = { rowPickerExpanded[i] = false }
                ) {
                    SettingsNavigateRow(
                        title = stringResource(R.string.demo_layout_row_n, i + 1),
                        value = stringResource(rowStyles[i].labelRes),
                        onClick = { rowPickerExpanded[i] = true }
                    )
                }
            }
        }

        // ═══ 实时预览 ═══
        SettingsSectionTitle(stringResource(R.string.demo_layout_section_preview))

        // 把行分为"卡片内行"和"独立容器行"
        val containerStyles = setOf(RowStyle.Collapse, RowStyle.SwitchCollapse, RowStyle.InputField)
        val cardRows = (0 until cardCount.coerceAtMost(maxRows)).filter {
            rowStyles[it] !in containerStyles
        }
        val containerRows = (0 until cardCount.coerceAtMost(maxRows)).filter {
            rowStyles[it] in containerStyles
        }

        // 卡片内行
        val valueOptions = listOf(stringResource(R.string.demo_layout_value_auto), stringResource(R.string.demo_layout_value_manual), stringResource(R.string.demo_layout_value_follow_system))
        if (cardRows.isNotEmpty()) {
            SettingsCard {
                cardRows.forEach { i ->
                    val sub = if (showSubtitle) stringResource(R.string.demo_layout_subtitle_sample) else null
                    when (rowStyles[i]) {
                        RowStyle.Switch -> {
                            SettingsSwitchRow(
                                title = stringResource(R.string.demo_layout_switch_n, i + 1),
                                subtitle = sub,
                                checked = switchStates[i],
                                onCheckedChange = { switchStates[i] = it }
                            )
                        }
                        RowStyle.Navigate -> {
                            SettingsNavigateRow(title = stringResource(R.string.demo_layout_navigate_n, i + 1), value = stringResource(R.string.demo_layout_navigate_value), onClick = {})
                        }
                        RowStyle.Value -> {
                            AnchoredPopupMenuBox(
                                options = valueOptions,
                                selected = valueSelections[i],
                                expanded = valuePopups[i],
                                onSelect = { valueSelections[i] = it; valuePopups[i] = false },
                                onDismiss = { valuePopups[i] = false }
                            ) {
                                SettingsValueRow(
                                    title = stringResource(R.string.demo_layout_value_row_n, i + 1),
                                    subtitle = if (showSubtitle) stringResource(R.string.demo_layout_value_row_subtitle) else null,
                                    value = valueSelections[i],
                                    onClick = { valuePopups[i] = true }
                                )
                            }
                        }
                        RowStyle.Text -> {
                            SettingsTextRow(title = stringResource(R.string.demo_layout_text_n, i + 1), subtitle = sub)
                        }
                        RowStyle.IconSwitch -> {
                            SettingsIconSwitchRow(
                                title = stringResource(R.string.demo_layout_icon_switch_n, i + 1),
                                subtitle = sub,
                                checked = switchStates[i],
                                onCheckedChange = { switchStates[i] = it },
                                leadingIcon = { PermissionIcon(iconRes = R.drawable.ic_feature_eye) }
                            )
                        }
                        RowStyle.IconNavigate -> {
                            SettingsIconNavigateRow(
                                title = stringResource(R.string.demo_layout_icon_navigate_n, i + 1),
                                subtitle = sub,
                                value = stringResource(R.string.demo_layout_icon_navigate_value),
                                onClick = {},
                                leadingIcon = { PermissionIcon(iconRes = R.drawable.ic_settings, color = GoldjucXColors.onSurfaceTertiary) }
                            )
                        }
                        RowStyle.Action -> {
                            SettingsActionRow(title = stringResource(R.string.demo_layout_action_n, i + 1), onClick = {}) {
                                CircleIconButton(iconRes = R.drawable.ic_add_circle, contentDescription = stringResource(R.string.demo_layout_action_add), onClick = {})
                            }
                        }
                        RowStyle.Slider -> {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                                SliderSettingRow(
                                    title = stringResource(R.string.demo_layout_slider_n, i + 1),
                                    subtitle = "${(sliderValue * 100).toInt()}%",
                                    value = sliderValue,
                                    onValueChange = { sliderValue = it },
                                    onReset = { sliderValue = 0.5f }
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // 独立容器行（折叠类组件自带卡片）
        containerRows.forEach { i ->
            when (rowStyles[i]) {
                RowStyle.Collapse -> {
                    CollapsibleSection(
                        title = stringResource(R.string.demo_layout_collapse_n, i + 1),
                        subtitle = if (showSubtitle) stringResource(R.string.demo_layout_collapse_subtitle) else null,
                        expanded = collapseStates[i],
                        onToggle = { collapseStates[i] = !collapseStates[i] }
                    ) {
                        SettingsNavigateRow(title = stringResource(R.string.demo_layout_collapse_child_a), onClick = {})
                        SettingsNavigateRow(title = stringResource(R.string.demo_layout_collapse_child_b), onClick = {})
                    }
                }
                RowStyle.SwitchCollapse -> {
                    SwitchCollapseSection(
                        title = stringResource(R.string.demo_layout_switch_collapse_n, i + 1),
                        subtitle = if (showSubtitle) stringResource(R.string.demo_layout_switch_collapse_subtitle) else null,
                        checked = switchStates[i],
                        onCheckedChange = { switchStates[i] = it }
                    ) {
                        SettingsCard {
                            SettingsNavigateRow(title = stringResource(R.string.demo_layout_switch_collapse_child_a), value = stringResource(R.string.demo_layout_value_auto), onClick = {})
                            SettingsNavigateRow(title = stringResource(R.string.demo_layout_switch_collapse_child_b), onClick = {})
                        }
                    }
                }
                RowStyle.InputField -> {
                    SettingsInputField(
                        label = stringResource(R.string.demo_layout_input_n, i + 1),
                        value = inputValues[i],
                        onValueChange = { inputValues[i] = it },
                        placeholder = stringResource(R.string.demo_layout_input_placeholder)
                    )
                }
                else -> {}
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Demo: 底部抽屉（递归堆叠测试）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun DemoBottomSheetDrawer(onBack: () -> Unit) {
    var showDrawer by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalDrawerStack provides remember { DrawerStackState() }) {
        Box(Modifier.fillMaxSize()) {
            SettingsPage(
                title = stringResource(R.string.demo_drawer_title),
                onBack = onBack
            ) {
                SettingsSectionTitle(stringResource(R.string.demo_drawer_section_stack_test))
                SettingsCard {
                    SettingsNavigateRow(
                        title = stringResource(R.string.demo_drawer_open),
                        value = stringResource(R.string.demo_drawer_open_value),
                        onClick = { showDrawer = true }
                    )
                }
                SettingsHintText(stringResource(R.string.demo_drawer_hint))
            }

            if (showDrawer) {
                StackedDrawerTest(level = 1, material = "frost", onClose = { showDrawer = false })
            }
        }
    }
}

@Composable
private fun StackedDrawerTest(level: Int, material: String, heightFraction: Float = 0.93f, headerIconKey: String = "close", showHandleInit: Boolean = true, dismissOnPullInit: Boolean = true, onClose: () -> Unit) {
    var showNext by remember { mutableStateOf(false) }

    // ─── 当前层参数（实时生效） ───
    var curMaterial by remember { mutableStateOf(material) }
    var curHeight by remember { mutableFloatStateOf(heightFraction) }
    var curHeaderIcon by remember { mutableStateOf(headerIconKey) }
    var curShowHandle by remember { mutableStateOf(showHandleInit) }
    var curDismissOnPull by remember { mutableStateOf(dismissOnPullInit) }
    var curBlurRadius by remember { mutableFloatStateOf(40f) }
    val levelTitle = stringResource(R.string.demo_drawer_level_title, level)
    var curTitle by remember { mutableStateOf(levelTitle) }
    var curTrailingCount by remember { mutableIntStateOf(0) }

    // ─── 下一层参数 ───
    var nextMaterial by remember { mutableStateOf("frost") }
    var nextHeight by remember { mutableFloatStateOf(0.93f) }
    var nextHeaderIcon by remember { mutableStateOf("close") }
    var nextShowHandle by remember { mutableStateOf(true) }
    var nextDismissOnPull by remember { mutableStateOf(true) }

    // ─── Picker 状态 ───
    var showMaterialPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showTrailingCountPicker by remember { mutableStateOf(false) }
    var showNextMaterialPicker by remember { mutableStateOf(false) }
    var showNextIconPicker by remember { mutableStateOf(false) }

    val sheetBackground = when (curMaterial) {
        "frost" -> SheetBackground.Blur(curBlurRadius.dp)
        "white" -> SheetBackground.White
        else -> SheetBackground.Gray
    }

    val resolvedIcon = when (curHeaderIcon) {
        "back" -> R.drawable.ic_back
        "close" -> R.drawable.ic_close
        else -> null
    }

    val trailingIconRes = listOf(R.drawable.ic_settings, R.drawable.ic_star, R.drawable.ic_person)

    BottomSheetDrawer(
        onClose = onClose,
        showHandle = curShowHandle,
        background = sheetBackground,
        heightFraction = curHeight,
        dismissOnPullDown = curDismissOnPull,
        headerIcon = resolvedIcon,
        title = curTitle,
        trailingIcons = if (curTrailingCount > 0) ({
            trailingIconRes.take(curTrailingCount).forEach { iconRes ->
                CircleIconButton(iconRes = iconRes, contentDescription = "", onClick = {})
            }
        }) else null
    ) { animateOut ->
        val scope = rememberCoroutineScope()
        val stack = LocalDrawerStack.current

        // ═══ 当前层配置 ═══
        val matGray = stringResource(R.string.demo_drawer_material_gray)
        val matFrost = stringResource(R.string.demo_drawer_material_frost)
        val matWhite = stringResource(R.string.demo_drawer_material_white)
        val iconBack = stringResource(R.string.demo_drawer_icon_back)
        val iconClose = stringResource(R.string.demo_drawer_icon_close)
        val iconNone = stringResource(R.string.demo_drawer_icon_none)

        SettingsSectionTitle(stringResource(R.string.demo_drawer_section_current))

        SettingsCard {
            AnchoredPopupMenuBox(
                options = listOf(matGray, matFrost, matWhite),
                selected = when(curMaterial) { "frost" -> matFrost; "white" -> matWhite; else -> matGray },
                expanded = showMaterialPicker,
                onSelect = { label ->
                    curMaterial = when(label) { matFrost -> "frost"; matWhite -> "white"; else -> "default" }
                    showMaterialPicker = false
                },
                onDismiss = { showMaterialPicker = false }
            ) {
                SettingsNavigateRow(title = "background", value = when(curMaterial) { "frost" -> "Blur"; "white" -> "White"; else -> "Gray" }, onClick = { showMaterialPicker = true })
            }
            AnchoredPopupMenuBox(
                options = listOf(iconBack, iconClose, iconNone),
                selected = when(curHeaderIcon) { "back" -> iconBack; "close" -> iconClose; else -> iconNone },
                expanded = showIconPicker,
                onSelect = { label ->
                    curHeaderIcon = when(label) { iconBack -> "back"; iconClose -> "close"; else -> "none" }
                    showIconPicker = false
                },
                onDismiss = { showIconPicker = false }
            ) {
                SettingsNavigateRow(title = "headerIcon", value = when(curHeaderIcon) { "back" -> "ic_back"; "close" -> "ic_close"; else -> "null" }, onClick = { showIconPicker = true })
            }
            AnchoredPopupMenuBox(
                options = listOf("0", "1", "2", "3"),
                selected = curTrailingCount.toString(),
                expanded = showTrailingCountPicker,
                onSelect = { label ->
                    curTrailingCount = label.toIntOrNull() ?: 0
                    showTrailingCountPicker = false
                },
                onDismiss = { showTrailingCountPicker = false }
            ) {
                SettingsNavigateRow(title = "trailingIcons", value = stringResource(R.string.demo_drawer_trailing_count, curTrailingCount), onClick = { showTrailingCountPicker = true })
            }
            SettingsSwitchRow(title = "showHandle", checked = curShowHandle, onCheckedChange = { curShowHandle = it })
            SettingsSwitchRow(title = "dismissOnPullDown", checked = curDismissOnPull, onCheckedChange = { curDismissOnPull = it })
        }

        SliderSettingSection(
            sectionTitle = "heightFraction",
            description = "${(curHeight * 100).toInt()}%",
            value = (curHeight - 0.3f) / 0.64f,
            onValueChange = { curHeight = (it * 0.64f + 0.3f).coerceIn(0.3f, 0.94f) }
        )

        if (curMaterial == "frost") {
            SliderSettingSection(
                sectionTitle = "blurRadius",
                description = "${curBlurRadius.toInt()}dp",
                value = curBlurRadius / 60f,
                onValueChange = { curBlurRadius = (it * 60f).coerceIn(10f, 60f) }
            )
        }

        // ═══ 下一层配置 ═══
        SettingsSectionTitle(stringResource(R.string.demo_drawer_section_next))

        SettingsCard {
            AnchoredPopupMenuBox(
                options = listOf(matGray, matFrost, matWhite),
                selected = when(nextMaterial) { "frost" -> matFrost; "white" -> matWhite; else -> matGray },
                expanded = showNextMaterialPicker,
                onSelect = { label ->
                    nextMaterial = when(label) { matFrost -> "frost"; matWhite -> "white"; else -> "default" }
                    showNextMaterialPicker = false
                },
                onDismiss = { showNextMaterialPicker = false }
            ) {
                SettingsNavigateRow(title = "background", value = when(nextMaterial) { "frost" -> "Blur"; "white" -> "White"; else -> "Gray" }, onClick = { showNextMaterialPicker = true })
            }
            AnchoredPopupMenuBox(
                options = listOf(iconBack, iconClose, iconNone),
                selected = when(nextHeaderIcon) { "back" -> iconBack; "close" -> iconClose; else -> iconNone },
                expanded = showNextIconPicker,
                onSelect = { label ->
                    nextHeaderIcon = when(label) { iconBack -> "back"; iconClose -> "close"; else -> "none" }
                    showNextIconPicker = false
                },
                onDismiss = { showNextIconPicker = false }
            ) {
                SettingsNavigateRow(title = "headerIcon", value = when(nextHeaderIcon) { "back" -> "ic_back"; "close" -> "ic_close"; else -> "null" }, onClick = { showNextIconPicker = true })
            }
            SettingsSwitchRow(title = "showHandle", checked = nextShowHandle, onCheckedChange = { nextShowHandle = it })
            SettingsSwitchRow(title = "dismissOnPullDown", checked = nextDismissOnPull, onCheckedChange = { nextDismissOnPull = it })
        }

        SliderSettingSection(
            sectionTitle = "heightFraction",
            description = "${(nextHeight * 100).toInt()}%",
            value = (nextHeight - 0.3f) / 0.64f,
            onValueChange = { nextHeight = (it * 0.64f + 0.3f).coerceIn(0.3f, 0.94f) }
        )

        // ═══ 操作按钮 ═══
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(text = stringResource(R.string.demo_drawer_btn_open_next), onClick = { showNext = true })
            SecondaryButton(text = stringResource(R.string.demo_drawer_btn_close_current), onClick = { scope.launch { animateOut(); onClose() } })
            SecondaryButton(text = stringResource(R.string.demo_drawer_btn_close_all), onClick = { stack.dismissAll() })
        }
    }

    if (showNext) {
        StackedDrawerTest(
            level = level + 1,
            material = nextMaterial,
            heightFraction = nextHeight,
            headerIconKey = nextHeaderIcon,
            showHandleInit = nextShowHandle,
            dismissOnPullInit = nextDismissOnPull,
            onClose = { showNext = false }
        )
    }
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Demo: GoldjucX 对话框 All Cases
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun DemoGoldjucXDialog(onBack: () -> Unit) {
    var showBasic by remember { mutableStateOf(false) }
    var showSingleButton by remember { mutableStateOf(false) }
    var showDanger by remember { mutableStateOf(false) }
    var showVertical3 by remember { mutableStateOf(false) }
    var showImage by remember { mutableStateOf(false) }
    var showCaption by remember { mutableStateOf(false) }
    var showInput by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }
    var showSingleSelect by remember { mutableStateOf(false) }
    val defaultMode = stringResource(R.string.demo_dialog_mode_standard)
    var singleSelected by remember { mutableStateOf(defaultMode) }
    var showMultiSelect by remember { mutableStateOf(false) }
    val defaultCategory = stringResource(R.string.demo_dialog_category_work)
    var multiSelected by remember { mutableStateOf(setOf(defaultCategory)) }
    var showGrayBg by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var contentSlider by remember { mutableFloatStateOf(0.5f) }
    var contentSwitch by remember { mutableStateOf(true) }
    var contentInputText by remember { mutableStateOf("") }
    val contentOptions = listOf(stringResource(R.string.demo_dialog_custom_text), stringResource(R.string.demo_dialog_custom_input), stringResource(R.string.demo_dialog_custom_slider), stringResource(R.string.demo_dialog_custom_switch), stringResource(R.string.demo_dialog_custom_image), stringResource(R.string.demo_dialog_custom_button))
    val contentEnabled = remember { mutableStateListOf(true, false, false, false, false, false) }
    var showSelectionSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        SettingsPage(
            title = stringResource(R.string.demo_dialog_title),
            onBack = onBack
        ) {
            SettingsSectionTitle(stringResource(R.string.demo_dialog_section_button_layout))
            SettingsCard {
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_two_button), value = stringResource(R.string.demo_dialog_two_button_value), onClick = { showBasic = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_single_button), value = stringResource(R.string.demo_dialog_single_button_value), onClick = { showSingleButton = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_danger), value = stringResource(R.string.demo_dialog_danger_value), onClick = { showDanger = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_three_button), value = stringResource(R.string.demo_dialog_three_button_value), onClick = { showVertical3 = true })
            }

            SettingsSectionTitle(stringResource(R.string.demo_dialog_section_content_type))
            SettingsCard {
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_with_image), value = "imageRes", onClick = { showImage = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_with_caption), value = "caption", onClick = { showCaption = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_with_input), value = "DialogInput", onClick = { showInput = true })
            }

            SettingsSectionTitle(stringResource(R.string.demo_dialog_section_custom_content))
            SettingsCard {
                contentOptions.forEachIndexed { idx, label ->
                    SettingsSwitchRow(
                        title = label,
                        checked = contentEnabled[idx],
                        onCheckedChange = { contentEnabled[idx] = it }
                    )
                }
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_preview), value = stringResource(R.string.demo_dialog_preview_count, contentEnabled.count { it }), onClick = { showContent = true })
            }

            SettingsSectionTitle(stringResource(R.string.demo_dialog_section_selection))
            SettingsCard {
                SettingsValueRow(title = stringResource(R.string.demo_dialog_single_select), subtitle = "DialogSelection.Single", value = singleSelected, onClick = { showSingleSelect = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_multi_select), value = "DialogSelection.Multi", onClick = { showMultiSelect = true })
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_selection_sheet), value = "SelectionBottomSheet", onClick = { showSelectionSheet = true })
            }

            SettingsSectionTitle(stringResource(R.string.demo_dialog_section_variants))
            SettingsCard {
                SettingsNavigateRow(title = stringResource(R.string.demo_dialog_gray_bg), value = "DialogBackground.Gray", onClick = { showGrayBg = true })
            }
        }

        // ═══ 对话框实例 ═══

        if (showBasic) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_basic_title),
                message = stringResource(R.string.demo_dialog_basic_message),
                confirmText = stringResource(R.string.demo_dialog_btn_confirm),
                dismissText = stringResource(R.string.demo_dialog_btn_cancel),
                onConfirm = { showBasic = false },
                onDismiss = { showBasic = false }
            )
        }

        if (showSingleButton) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_complete_title),
                buttons = listOf(DialogButton("ok", stringResource(R.string.demo_dialog_btn_got_it), ButtonType.Primary)),
                onButtonClick = { showSingleButton = false },
                onDismiss = { showSingleButton = false },
                message = stringResource(R.string.demo_dialog_complete_message)
            )
        }

        if (showDanger) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_delete_title),
                buttons = listOf(
                    DialogButton("cancel", stringResource(R.string.demo_dialog_btn_cancel), ButtonType.Normal),
                    DialogButton("delete", stringResource(R.string.demo_dialog_btn_delete), ButtonType.Danger)
                ),
                onButtonClick = { showDanger = false },
                onDismiss = { showDanger = false },
                message = stringResource(R.string.demo_dialog_delete_message)
            )
        }

        if (showVertical3) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_choose_title),
                buttons = listOf(
                    DialogButton("save", stringResource(R.string.demo_dialog_btn_save_exit), ButtonType.Primary),
                    DialogButton("discard", stringResource(R.string.demo_dialog_btn_discard_exit), ButtonType.Normal),
                    DialogButton("cancel", stringResource(R.string.demo_dialog_btn_cancel), ButtonType.Normal)
                ),
                onButtonClick = { showVertical3 = false },
                onDismiss = { showVertical3 = false },
                message = stringResource(R.string.demo_dialog_choose_message)
            )
        }

        if (showImage) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_image_title),
                buttons = listOf(
                    DialogButton("dismiss", stringResource(R.string.demo_dialog_btn_cancel), ButtonType.Normal),
                    DialogButton("confirm", stringResource(R.string.demo_dialog_btn_enable), ButtonType.Primary)
                ),
                onButtonClick = { showImage = false },
                onDismiss = { showImage = false },
                message = stringResource(R.string.demo_dialog_image_message),
                imageRes = R.drawable.illustration_control_center
            )
        }

        if (showCaption) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_caption_title),
                buttons = listOf(
                    DialogButton("dismiss", stringResource(R.string.demo_dialog_btn_reject), ButtonType.Normal),
                    DialogButton("accept", stringResource(R.string.demo_dialog_btn_accept), ButtonType.Primary)
                ),
                onButtonClick = { showCaption = false },
                onDismiss = { showCaption = false },
                message = stringResource(R.string.demo_dialog_caption_message),
                caption = stringResource(R.string.demo_dialog_caption_text)
            )
        }

        if (showInput) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_input_title),
                buttons = listOf(
                    DialogButton("cancel", stringResource(R.string.demo_dialog_btn_cancel), ButtonType.Normal),
                    DialogButton("save", stringResource(R.string.demo_dialog_btn_save), ButtonType.Primary, enabled = inputValue.isNotBlank())
                ),
                onButtonClick = { showInput = false },
                onDismiss = { showInput = false },
                input = DialogInput(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    placeholder = stringResource(R.string.demo_dialog_input_placeholder),
                    label = stringResource(R.string.demo_dialog_input_label)
                )
            )
        }

        if (showSingleSelect) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_select_mode_title),
                buttons = listOf(DialogButton("done", stringResource(R.string.demo_dialog_btn_done), ButtonType.Primary)),
                onButtonClick = { showSingleSelect = false },
                onDismiss = { showSingleSelect = false },
                selection = DialogSelection.Single(
                    options = listOf(stringResource(R.string.demo_dialog_mode_standard), stringResource(R.string.demo_dialog_mode_power_save), stringResource(R.string.demo_dialog_mode_performance), stringResource(R.string.demo_dialog_mode_balanced)),
                    selected = singleSelected,
                    onSelect = { singleSelected = it }
                )
            )
        }

        if (showMultiSelect) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_select_category_title),
                buttons = listOf(
                    DialogButton("cancel", stringResource(R.string.demo_dialog_btn_cancel), ButtonType.Normal),
                    DialogButton("done", stringResource(R.string.demo_dialog_btn_complete), ButtonType.Primary)
                ),
                onButtonClick = { showMultiSelect = false },
                onDismiss = { showMultiSelect = false },
                message = stringResource(R.string.demo_dialog_select_category_message),
                selection = DialogSelection.Multi(
                    options = listOf(stringResource(R.string.demo_dialog_category_work), stringResource(R.string.demo_dialog_category_social), stringResource(R.string.demo_dialog_category_finance), stringResource(R.string.demo_dialog_category_life), stringResource(R.string.demo_dialog_category_entertainment)),
                    selected = multiSelected,
                    onToggle = { opt ->
                        multiSelected = if (opt in multiSelected) multiSelected - opt else multiSelected + opt
                    }
                )
            )
        }

        if (showGrayBg) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_hint_title),
                buttons = listOf(DialogButton("ok", stringResource(R.string.demo_dialog_btn_ok), ButtonType.Primary)),
                onButtonClick = { showGrayBg = false },
                onDismiss = { showGrayBg = false },
                message = stringResource(R.string.demo_dialog_hint_message),
                background = DialogBackground.Gray
            )
        }

        if (showContent) {
            GoldjucXDialog(
                title = stringResource(R.string.demo_dialog_custom_preview_title),
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (contentEnabled[0]) {
                            Text(stringResource(R.string.demo_dialog_custom_sample_text), style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = GoldjucXColors.onSurfaceSecondary, lineHeight = 22.sp))
                        }
                        if (contentEnabled[1]) {
                            SettingsInputField(
                                label = stringResource(R.string.demo_dialog_custom_input_label),
                                value = contentInputText,
                                onValueChange = { contentInputText = it },
                                placeholder = stringResource(R.string.demo_dialog_custom_input_placeholder),
                                maxVisibleLines = 2
                            )
                        }
                        if (contentEnabled[2]) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(R.string.demo_dialog_custom_slider_label, (contentSlider * 100).toInt()), style = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = GoldjucXColors.onSurfaceTertiary))
                                GoldjucXSlider(value = contentSlider, onValueChange = { contentSlider = it })
                            }
                        }
                        if (contentEnabled[3]) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.demo_dialog_custom_switch_label), style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = GoldjucXColors.onSurface))
                                GoldjucXSwitch(checked = contentSwitch, onCheckedChange = { contentSwitch = it })
                            }
                        }
                        if (contentEnabled[4]) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.illustration_peep_guard),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                            )
                        }
                        if (contentEnabled[5]) {
                            PrimaryButton(text = stringResource(R.string.demo_dialog_custom_btn_text), onClick = {})
                        }
                        if (contentEnabled.none { it }) {
                            Text(stringResource(R.string.demo_dialog_custom_empty), style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary))
                        }
                    }
                },
                confirmText = stringResource(R.string.demo_dialog_btn_close),
                dismissText = stringResource(R.string.demo_dialog_btn_cancel),
                onConfirm = { showContent = false },
                onDismiss = { showContent = false }
            )
        }

        if (showSelectionSheet) {
            SelectionBottomSheet(
                title = stringResource(R.string.demo_dialog_sheet_title),
                options = listOf(stringResource(R.string.demo_dialog_sheet_work), stringResource(R.string.demo_dialog_sheet_life), stringResource(R.string.demo_dialog_sheet_study), stringResource(R.string.demo_dialog_sheet_entertainment), stringResource(R.string.demo_dialog_sheet_other)),
                onSelect = { showSelectionSheet = false },
                onDismiss = { showSelectionSheet = false }
            )
        }
    }
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Demo: 通用组件（交互配置）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun DemoCommonComponents(onBack: () -> Unit) {
    // CircleIconButton config
    var btnEnabled by remember { mutableStateOf(true) }
    val tintLabels = listOf(stringResource(R.string.demo_components_tint_default), stringResource(R.string.demo_components_tint_primary), stringResource(R.string.demo_components_tint_red), stringResource(R.string.demo_components_tint_gray))
    val tintColors = listOf(GoldjucXColors.onSurface, GoldjucXColors.primary, Color(0xFFE53935), GoldjucXColors.onSurfaceTertiary)
    val tintOptions = tintLabels.zip(tintColors)
    var tintIndex by remember { mutableIntStateOf(0) }
    var showTintMenu by remember { mutableStateOf(false) }

    // PrimaryButton config
    var primaryEnabled by remember { mutableStateOf(true) }
    var primaryCompact by remember { mutableStateOf(false) }

    // SecondaryButton config
    val secLabels = listOf(stringResource(R.string.demo_components_sec_default), stringResource(R.string.demo_components_sec_primary), stringResource(R.string.demo_components_sec_red))
    val secColors = listOf(GoldjucXColors.onSurfaceSecondary, GoldjucXColors.primary, Color(0xFFE53935))
    val secColorOptions = secLabels.zip(secColors)
    var secColorIndex by remember { mutableIntStateOf(0) }
    var showSecColorMenu by remember { mutableStateOf(false) }

    // SegmentedTabBar config
    var segTabSlider by remember { mutableFloatStateOf(1f / 3f) }
    val segTabCount = 2 + (segTabSlider * 3f + 0.5f).toInt().coerceIn(0, 3)
    var segmentIndex by remember { mutableIntStateOf(0) }
    var segGlassMode by remember { mutableStateOf(false) }

    // GoldjucXSwitch config
    var switchChecked by remember { mutableStateOf(true) }

    // GoldjucXSlider config
    var sliderValue by remember { mutableFloatStateOf(0.5f) }

    SettingsPage(
        title = stringResource(R.string.demo_components_title),
        onBack = onBack
    ) {
            // ═══ CircleIconButton ═══
            SettingsSectionTitle(stringResource(R.string.demo_components_section_circle_btn))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleIconButton(
                    iconRes = com.goldjucx.ui.R.drawable.ic_back,
                    contentDescription = stringResource(R.string.demo_components_cd_back),
                    onClick = {},
                    tint = tintOptions[tintIndex].second,
                    enabled = btnEnabled
                )
                CircleIconButton(
                    iconRes = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.demo_components_cd_close),
                    onClick = {},
                    tint = tintOptions[tintIndex].second,
                    enabled = btnEnabled
                )
                CircleIconButton(
                    iconRes = R.drawable.ic_settings,
                    contentDescription = stringResource(R.string.demo_components_cd_settings),
                    onClick = {},
                    tint = tintOptions[tintIndex].second,
                    enabled = btnEnabled
                )
            }
            SettingsCard {
                SettingsSwitchRow(
                    title = "enabled",
                    subtitle = stringResource(R.string.demo_components_btn_enabled_subtitle),
                    checked = btnEnabled,
                    onCheckedChange = { btnEnabled = it }
                )
                AnchoredPopupMenuBox(
                    options = tintOptions.map { it.first },
                    selected = tintOptions[tintIndex].first,
                    expanded = showTintMenu,
                    onSelect = { name -> tintIndex = tintOptions.indexOfFirst { it.first == name }; showTintMenu = false },
                    onDismiss = { showTintMenu = false }
                ) {
                    SettingsValueRow(
                        title = "tint",
                        subtitle = stringResource(R.string.demo_components_tint_subtitle),
                        value = tintOptions[tintIndex].first,
                        onClick = { showTintMenu = true }
                    )
                }
            }

            // ═══ PrimaryButton ═══
            SettingsSectionTitle(stringResource(R.string.demo_components_section_primary_btn))
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                PrimaryButton(
                    text = if (primaryCompact) stringResource(R.string.demo_components_primary_compact) else stringResource(R.string.demo_components_primary_normal),
                    onClick = {},
                    enabled = primaryEnabled,
                    compact = primaryCompact
                )
            }
            SettingsCard {
                SettingsSwitchRow(title = "enabled", checked = primaryEnabled, onCheckedChange = { primaryEnabled = it })
                SettingsSwitchRow(title = "compact", subtitle = stringResource(R.string.demo_components_compact_subtitle), checked = primaryCompact, onCheckedChange = { primaryCompact = it })
            }

            // ═══ SecondaryButton ═══
            SettingsSectionTitle(stringResource(R.string.demo_components_section_secondary_btn))
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                SecondaryButton(
                    text = stringResource(R.string.demo_components_secondary_text),
                    onClick = {},
                    textColor = secColorOptions[secColorIndex].second
                )
            }
            SettingsCard {
                AnchoredPopupMenuBox(
                    options = secColorOptions.map { it.first },
                    selected = secColorOptions[secColorIndex].first,
                    expanded = showSecColorMenu,
                    onSelect = { name -> secColorIndex = secColorOptions.indexOfFirst { it.first == name }; showSecColorMenu = false },
                    onDismiss = { showSecColorMenu = false }
                ) {
                    SettingsValueRow(
                        title = "textColor",
                        subtitle = stringResource(R.string.demo_components_text_color_subtitle),
                        value = secColorOptions[secColorIndex].first,
                        onClick = { showSecColorMenu = true }
                    )
                }
            }

            // ═══ GoldjucXSwitch ═══
            SettingsSectionTitle("GoldjucXSwitch")
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                GoldjucXSwitch(checked = switchChecked, onCheckedChange = { switchChecked = it })
            }

            // ═══ GoldjucXSlider ═══
            SettingsSectionTitle("GoldjucXSlider")
            Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                GoldjucXSlider(value = sliderValue, onValueChange = { sliderValue = it })
            }

            // ═══ PermissionIcon ═══
            SettingsSectionTitle("PermissionIcon")
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionIcon(iconRes = R.drawable.ic_settings, color = GoldjucXColors.primary)
                PermissionIcon(iconRes = R.drawable.ic_star, color = Color(0xFFE53935))
                PermissionIcon(iconRes = R.drawable.ic_person, color = Color(0xFF4CAF50))
                PermissionIcon(iconRes = R.drawable.ic_close, color = Color(0xFFFF9800))
            }

            // ═══ SegmentedTabBar ═══
            SettingsSectionTitle(stringResource(R.string.demo_components_section_segmented))
            val allSegTabs = listOf(stringResource(R.string.demo_components_seg_all), stringResource(R.string.demo_components_seg_unread), stringResource(R.string.demo_components_seg_read), stringResource(R.string.demo_components_seg_favorite), stringResource(R.string.demo_components_seg_archive))
            val activeSegTabs = allSegTabs.take(segTabCount)
            if (segmentIndex >= activeSegTabs.size) segmentIndex = activeSegTabs.size - 1
            Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                SegmentedTabBar(
                    tabs = activeSegTabs,
                    selectedIndex = segmentIndex,
                    onTabSelected = { segmentIndex = it },
                    glassMode = segGlassMode
                )
            }
            SettingsCard {
                SettingsSwitchRow(
                    title = "Glass 模式",
                    subtitle = "glassMode",
                    checked = segGlassMode,
                    onCheckedChange = { segGlassMode = it }
                )
            }
            SliderSettingSection(
                sectionTitle = stringResource(R.string.demo_components_tab_count, segTabCount),
                description = stringResource(R.string.demo_components_tab_count_desc),
                value = segTabSlider,
                onValueChange = { segTabSlider = it }
            )

            // ═══ CodeBlock ═══
            SettingsSectionTitle(stringResource(R.string.demo_components_section_code_block))
            Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                CodeBlock(
                    code = """val dialog = GoldjucXDialog(
    title = "确认操作",
    message = "确定要执行吗？",
    onConfirm = { /* ... */ },
    onDismiss = { /* ... */ }
)"""
                )
            }

            // ═══ FeatureListItem ═══
            SettingsSectionTitle(stringResource(R.string.demo_components_section_feature_item))
            SettingsCard {
                FeatureListItem(iconRes = R.drawable.ic_star, title = stringResource(R.string.demo_components_feature_smart), subtitle = stringResource(R.string.demo_components_feature_smart_sub))
                FeatureListItem(iconRes = R.drawable.ic_settings, title = stringResource(R.string.demo_components_feature_custom), subtitle = stringResource(R.string.demo_components_feature_custom_sub))
            }
    }
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Demo: 材质预览（Liquid Glass）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun DemoMaterialPreview(onBack: () -> Unit) {
    val presets = remember {
        listOf(LiquidGlassMaterial.Thin, LiquidGlassMaterial.Regular, LiquidGlassMaterial.Thick)
    }
    val savedDefaults = LiquidGlassDefaults.material

    var presetIndex by remember { mutableIntStateOf(3) }
    var blurRadius by remember { mutableFloatStateOf(savedDefaults.blurRadius) }
    var saturation by remember { mutableFloatStateOf(savedDefaults.saturation) }
    var tintStrength by remember { mutableFloatStateOf(savedDefaults.tintStrength) }
    var refractionStrength by remember { mutableFloatStateOf(savedDefaults.refractionStrength) }
    var lightAngleDeg by remember { mutableFloatStateOf(savedDefaults.lightAngleDeg) }
    var specularIntensity by remember { mutableFloatStateOf(savedDefaults.specularIntensity) }
    var specularWidth by remember { mutableFloatStateOf(savedDefaults.specularWidth) }
    var specularSharpness by remember { mutableFloatStateOf(savedDefaults.specularSharpness) }
    var backlightIntensity by remember { mutableFloatStateOf(savedDefaults.backlightIntensity) }
    var brightnessBoost by remember { mutableFloatStateOf(savedDefaults.brightnessBoost) }

    fun syncFromPreset(m: LiquidGlassMaterial) {
        blurRadius = m.blurRadius
        saturation = m.saturation
        tintStrength = m.tintStrength
        refractionStrength = m.refractionStrength
        lightAngleDeg = m.lightAngleDeg
        specularIntensity = m.specularIntensity
        specularWidth = m.specularWidth
        specularSharpness = m.specularSharpness
        backlightIntensity = m.backlightIntensity
        brightnessBoost = m.brightnessBoost
    }

    val material = when (presetIndex) {
        0 -> LiquidGlassMaterial.Thin
        1 -> LiquidGlassMaterial.Regular
        2 -> LiquidGlassMaterial.Thick
        else -> LiquidGlassMaterial(
            blurRadius = blurRadius,
            saturation = saturation,
            tintStrength = tintStrength,
            refractionStrength = refractionStrength,
            lightAngleDeg = lightAngleDeg,
            specularIntensity = specularIntensity,
            specularWidth = specularWidth,
            specularSharpness = specularSharpness,
            backlightIntensity = backlightIntensity,
            brightnessBoost = brightnessBoost
        )
    }

    var showDrawer by remember { mutableStateOf(false) }

    // 玻璃面板拖动偏移
    var glassDragX by remember { mutableFloatStateOf(0f) }
    var glassDragY by remember { mutableFloatStateOf(0f) }

    CompositionLocalProvider(LocalDrawerStack provides remember { DrawerStackState() }) {
        SettingsPage(
            title = "Liquid Glass 组件",
            onBack = onBack,
            backgroundColor = GoldjucXColors.surfaceLow
        ) { _ ->
            // 液态玻璃展示卡片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                // 液态玻璃 shader 层（背景 + 玻璃区域，文字不放在这里）
                LiquidGlassScene(
                    material = material,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 底板图片（尺寸跟随图片比例）
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.bg_liquid_glass),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                    )

                    // 玻璃面板（只定义玻璃区域，不放文字）
                    LiquidGlass(
                        cornerRadius = 20.dp,
                        modifier = Modifier
                            .offset {
                                androidx.compose.ui.unit.IntOffset(
                                    glassDragX.toInt(),
                                    glassDragY.toInt()
                                )
                            }
                            .align(Alignment.Center)
                            .size(180.dp, 120.dp)
                    ) {}
                }

                // 文字覆盖层（在 shader 外面，跟随玻璃面板位置）
                Box(
                    modifier = Modifier
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                glassDragX.toInt(),
                                glassDragY.toInt()
                            )
                        }
                        .align(Alignment.Center)
                        .size(180.dp, 120.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                glassDragX += dragAmount.x
                                glassDragY += dragAmount.y
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Liquid Glass",
                            style = TextStyle(fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "拖动我试试",
                            style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 材质预设选择
            SettingsSectionTitle("材质预设")
            SegmentedTabBar(
                tabs = listOf("Thin", "Regular", "Thick", "Custom"),
                selectedIndex = presetIndex,
                onTabSelected = { idx ->
                    presetIndex = idx
                    if (idx < 3) syncFromPreset(presets[idx])
                },
                glassMode = true,
                glassMaterial = material
            )

            Spacer(Modifier.height(16.dp))

            // 打开参数调节抽屉
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                PrimaryButton(
                    text = "调节参数",
                    onClick = { showDrawer = true }
                )
            }

            Spacer(Modifier.height(40.dp))
        }

        // 参数调节抽屉
        if (showDrawer) {
            BottomSheetDrawer(
                onClose = { showDrawer = false },
                heightFraction = 0.93f,
                title = "参数调节",
                showHandle = true
            ) { _ ->
                Column(Modifier.fillMaxWidth()) {
                    SettingsSectionTitle("光学效果")
                    SettingsCard {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            SliderSettingRow(
                                title = "模糊半径",
                                subtitle = "${blurRadius.toInt()} px",
                                value = blurRadius / 80f,
                                onValueChange = { blurRadius = it * 80f; presetIndex = 3 },
                                onReset = { blurRadius = savedDefaults.blurRadius; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "饱和度",
                                subtitle = "${"%.2f".format(saturation)}x",
                                value = (saturation - 0.5f) / 2.5f,
                                onValueChange = { saturation = it * 2.5f + 0.5f; presetIndex = 3 },
                                onReset = { saturation = savedDefaults.saturation; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "白色 tint",
                                subtitle = "${"%.2f".format(tintStrength)}",
                                value = tintStrength / 0.5f,
                                onValueChange = { tintStrength = it * 0.5f; presetIndex = 3 },
                                onReset = { tintStrength = savedDefaults.tintStrength; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "折射强度",
                                subtitle = "${refractionStrength.toInt()}",
                                value = refractionStrength / 100f,
                                onValueChange = { refractionStrength = it * 100f; presetIndex = 3 },
                                onReset = { refractionStrength = savedDefaults.refractionStrength; presetIndex = 3 }
                            )
                        }
                    }

                    SettingsSectionTitle("光照与高光")
                    SettingsCard {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            SliderSettingRow(
                                title = "光照角度",
                                subtitle = "${lightAngleDeg.toInt()}°",
                                value = lightAngleDeg / 360f,
                                onValueChange = { lightAngleDeg = it * 360f; presetIndex = 3 },
                                onReset = { lightAngleDeg = savedDefaults.lightAngleDeg; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "高光强度",
                                subtitle = "${"%.2f".format(specularIntensity)}",
                                value = specularIntensity / 1.5f,
                                onValueChange = { specularIntensity = it * 1.5f; presetIndex = 3 },
                                onReset = { specularIntensity = savedDefaults.specularIntensity; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "高光宽度",
                                subtitle = "${specularWidth.toInt()} px",
                                value = (specularWidth - 1f) / 29f,
                                onValueChange = { specularWidth = it * 29f + 1f; presetIndex = 3 },
                                onReset = { specularWidth = savedDefaults.specularWidth; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "高光锐度",
                                subtitle = "${"%.1f".format(specularSharpness)}",
                                value = (specularSharpness - 1f) / 19f,
                                onValueChange = { specularSharpness = it * 19f + 1f; presetIndex = 3 },
                                onReset = { specularSharpness = savedDefaults.specularSharpness; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "背光强度",
                                subtitle = "${"%.2f".format(backlightIntensity)}",
                                value = backlightIntensity / 0.5f,
                                onValueChange = { backlightIntensity = it * 0.5f; presetIndex = 3 },
                                onReset = { backlightIntensity = savedDefaults.backlightIntensity; presetIndex = 3 }
                            )
                            Spacer(Modifier.height(16.dp))
                            SliderSettingRow(
                                title = "亮度提升",
                                subtitle = "${"%.2f".format(brightnessBoost)}x",
                                value = (brightnessBoost - 0.9f) / 0.3f,
                                onValueChange = { brightnessBoost = it * 0.3f + 0.9f; presetIndex = 3 },
                                onReset = { brightnessBoost = savedDefaults.brightnessBoost; presetIndex = 3 }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton(
                        text = "设为全局默认材质",
                        onClick = { LiquidGlassDefaults.applyAndSave(material) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

