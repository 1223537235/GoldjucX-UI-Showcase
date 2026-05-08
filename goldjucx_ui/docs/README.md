# GoldjucX_UI 组件库 — AI 执行协议

> 本文件是 AI 生成代码的强制规范。读完即可调用所有 GoldjucX_UI 组件。各组件完整模板见 `components/` 子目录。

## AI 生成代码元规则（每次写代码前必须核对）

1. **所有颜色/阴影数值必须从 GoldjucXColors 取 Token** — 禁止凭记忆写 `Color(0x...)` 近似值
2. **每写完一个组件调用，回查对应 `components/*.md` 的"模板代码"章节**
3. **每写完一个页面，逐条跑底部"生成后自检清单"**
4. **使用 Android 高版本 API 时必须加 `Build.VERSION.SDK_INT` 保护**（minSdk = 26）
5. **所有弹出层必须处理系统返回键**（BackHandler）— BottomSheetDrawer / GoldjucXDialog / AnchoredPopupMenu 已内置
6. **禁止在 Demo/示例代码中硬编码坐标(dp/px)** — 必须通过 `onGloballyPositioned` 获取真实锚点

## 使用前提（app module 必须配置）

```xml
<!-- AndroidManifest.xml 必须声明 -->
<uses-permission android:name="android.permission.VIBRATE" />
```
原因：GoldjucXSwitch、GoldjucXSlider 均使用振动反馈，缺少权限会闪退。

## 引入方式

```kotlin
import com.goldjucx.ui.*
```

`goldjucx_ui` 是独立 library module，源码在 `src/main/java/com/miui/goldjucx_ui/`。

## 禁止使用规则

| 禁止使用 | 必须替换为 | 原因 |
|---------|----------|------|
| Material3 Switch | `GoldjucXSwitch` | 外观完全不同 |
| Material3 TopAppBar / Scaffold | `SettingsPage`（内置 CollapsibleTitleBar） | 必须弹性折叠 |
| Material3 AlertDialog | `GoldjucXDialog` | 必须底部滑入 |
| Material3 ModalBottomSheet | `BottomSheetDrawer` | 必须自定义动画 |
| Material3 DropdownMenu | `AnchoredPopupMenuBox` | 必须近手弹出 |
| Material3 Slider | `GoldjucXSlider` | 必须胶囊+振动 |
| 手写 Box + CircleShape 圆形按钮 | `CircleIconButton` | 44dp+8dp阴影+95%白 |
| Modifier.clickable 用于列表行 | `pressDimPublic` 或标准行组件 | 必须有按压缩放+压黑 |
| Divider / HorizontalDivider | 删除 | GoldjucX_UI 不用分割线 |

## 组件索引

| 组件 | 文档 | 用途 |
|------|------|------|
| GoldjucXColors | [components/GoldjucXColors.md](components/GoldjucXColors.md) | 色彩与阴影体系 |
| CommonComponents | [components/CommonComponents.md](components/CommonComponents.md) | 开关/圆形按钮/按钮组/分段选择器 |
| CollapsibleTitleBar | [components/CollapsibleTitleBar.md](components/CollapsibleTitleBar.md) | 弹性折叠标题栏（内部组件，由 SettingsPage 自动调用） |
| BottomSheetDrawer | [components/BottomSheetDrawer.md](components/BottomSheetDrawer.md) | 底部抽屉（支持多层叠加） |
| AnchoredPopupMenu | [components/AnchoredPopupMenu.md](components/AnchoredPopupMenu.md) | 近手弹窗菜单（推荐用 AnchoredPopupMenuBox） |
| GoldjucXDialog | [components/GoldjucXDialog.md](components/GoldjucXDialog.md) | 底部对话框 |
| SettingsLayout | [components/SettingsLayout.md](components/SettingsLayout.md) | 设置页布局组件集 |
| GlassBottomBar | [components/GlassBottomBar.md](components/GlassBottomBar.md) | 玻璃底部导航栏 |
| SpecialComponents | [components/SpecialComponents.md](components/SpecialComponents.md) | 滑块/Lottie/按压效果 |

---

## 页面模板（直接复制，不要改结构）

### 模板 A：标准设置页

```kotlin
@Composable
fun XxxSettingsScreen(onBack: () -> Unit) {
    var switchOn by remember { mutableStateOf(false) }

    SettingsPage(title = "页面标题", onBack = onBack) { _ ->
        SettingsSectionTitle("分组标题")
        SettingsCard {
            SettingsSwitchRow(title = "开关项", subtitle = "说明文字", checked = switchOn, onCheckedChange = { switchOn = it })
            SettingsNavigateRow(title = "导航项", value = "当前值", onClick = { /* 打开子页或抽屉 */ })
        }
        SettingsHintText("底部说明文字")
    }
}
```

### 模板 B：带抽屉的页面

```kotlin
@Composable
fun XxxScreenWithDrawer(onBack: () -> Unit) {
    var showSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalDrawerStack provides remember { DrawerStackState() }) {
        // ⚠️ 最外层 Box：无 statusBarsPadding，抽屉蒙层才能覆盖状态栏
        Box(Modifier.fillMaxSize()) {
            // ━━ 内容层 ━━
            SettingsPage(title = "标题", onBack = onBack) { _ ->
                SettingsCard {
                    SettingsNavigateRow(title = "打开抽屉", onClick = { showSheet = true })
                }
            }
            // ━━ 弹出层（与内容层平级，不在 statusBarsPadding 内）━━
            if (showSheet) {
                BottomSheetDrawer(onClose = { showSheet = false }, title = "抽屉标题") { animateOut ->
                    val scope = rememberCoroutineScope()
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SettingsCard {
                            SettingsNavigateRow(title = "子项", onClick = { /* ... */ })
                            SettingsSwitchRow(title = "开关", checked = false, onCheckedChange = { })
                        }
                        // 抽屉内关闭按钮示例：
                        PrimaryButton(text = "完成", onClick = {
                            scope.launch { animateOut(); showSheet = false }
                        })
                    }
                }
            }
        }
    }
}
```

### 模板 C：带多层抽屉的页面

```kotlin
@Composable
fun XxxScreenMultiDrawer(onBack: () -> Unit) {
    var showFirst by remember { mutableStateOf(false) }
    var showSecond by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalDrawerStack provides remember { DrawerStackState() }) {
        Box(Modifier.fillMaxSize()) {
            // ━━ 内容层 ━━
            SettingsPage(title = "标题", onBack = onBack) { _ ->
                SettingsCard {
                    SettingsNavigateRow(title = "打开第一层", onClick = { showFirst = true })
                }
            }
            // ━━ 抽屉层：所有抽屉平级排列，不嵌套 ━━
            if (showFirst) {
                BottomSheetDrawer(onClose = { showFirst = false }, title = "第一层") { animateOut ->
                    val scope = rememberCoroutineScope()
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SettingsCard {
                            SettingsNavigateRow(title = "打开第二层", onClick = { showSecond = true })
                            SettingsNavigateRow(title = "关闭当前", onClick = {
                                scope.launch { animateOut(); showFirst = false }
                            })
                        }
                    }
                }
            }
            if (showSecond) {
                BottomSheetDrawer(onClose = { showSecond = false }, title = "第二层") { animateOut ->
                    val scope = rememberCoroutineScope()
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SettingsCard {
                            SettingsNavigateRow(title = "内容", onClick = { })
                        }
                        PrimaryButton(text = "关闭当前", onClick = {
                            scope.launch { animateOut(); showSecond = false }
                        })
                    }
                }
            }
        }
    }
}
```

### 模板 D：带弹窗菜单的页面

```kotlin
@Composable
fun XxxScreenWithPopup(onBack: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var currentOption by remember { mutableStateOf("选项A") }

    Box(Modifier.fillMaxSize()) {
        SettingsPage(
            title = "标题",
            onBack = onBack,
            trailingContent = {
                AnchoredPopupMenuBox(
                    options = listOf("选项A", "选项B", "选项C"),
                    selected = currentOption,
                    expanded = showMenu,
                    onSelect = { currentOption = it; showMenu = false },
                    onDismiss = { showMenu = false }
                ) {
                    CircleIconButton(
                        iconRes = R.drawable.ic_more,
                        contentDescription = "更多",
                        onClick = { showMenu = true }
                    )
                }
            }
        ) { _ ->
            SettingsCard {
                SettingsNavigateRow(title = "当前选择", value = currentOption, onClick = { showMenu = true })
            }
        }
    }
}
```

### 模板 E：带对话框的页面

```kotlin
@Composable
fun XxxScreenWithDialog(onBack: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        SettingsPage(title = "标题", onBack = onBack) { _ ->
            SettingsCard {
                SettingsNavigateRow(title = "危险操作", onClick = { showDialog = true })
            }
        }
        // ━━ 对话框层（与内容层平级）━━
        if (showDialog) {
            GoldjucXDialog(
                title = "确认操作",
                message = "此操作不可撤销，确定继续吗？",
                confirmText = "确定",
                dismissText = "取消",
                onConfirm = { /* 执行操作 */; showDialog = false },
                onDismiss = { showDialog = false }
            )
        }
    }
}
```

### 模板 F：多 Tab 页面

```kotlin
@Composable
fun XxxTabbedScreen(onBack: () -> Unit) {
    SettingsPage(
        title = "标题",
        onBack = onBack,
        tabs = listOf(
            GlassTab("Tab1", R.drawable.ic_tab1),
            GlassTab("Tab2", R.drawable.ic_tab2)
        )
    ) { page ->
        when (page) {
            0 -> {
                SettingsSectionTitle("第一页")
                SettingsCard {
                    SettingsSwitchRow(title = "开关", checked = false, onCheckedChange = { })
                }
            }
            1 -> {
                SettingsSectionTitle("第二页")
                SettingsCard {
                    SettingsNavigateRow(title = "导航", onClick = { })
                }
            }
        }
    }
    // ⚠️ 不要在 content 里加 verticalScroll，已内置
}
```

---

## 生成后自检清单（必须逐条确认）

生成代码后，逐条检查以下内容。任何一条不通过则必须修正：

- [ ] **颜色**：是否所有颜色都从 `GoldjucXColors.xxx` 取？搜索 `Color(0x` 或 `Color.` 应为零结果（Color.White/Black/Transparent 除外）
- [ ] **弹出层位置**：BottomSheetDrawer / AnchoredPopupMenu / GoldjucXDialog 是否在 `statusBarsPadding` **外面**、与内容层**平级**？
- [ ] **抽屉关闭动画**：BottomSheetDrawer 的 content lambda 是否使用了 `animateOut`（不是 `_`）？关闭是否走 `scope.launch { animateOut(); showXxx = false }`？
- [ ] **多层抽屉**：是否所有抽屉在同一个 Box 内平级排列？是否没有嵌套在另一个抽屉的 content 里？
- [ ] **额外 padding**：抽屉/弹窗内部是否**没有**额外 `padding(horizontal = 16.dp)` 等？（SettingsCard 自带 12dp，行组件自带 16dp）
- [ ] **弹窗菜单**：是否使用 `AnchoredPopupMenuBox`（推荐）？如用底层 `AnchoredPopupMenu`，坐标是否通过 `onGloballyPositioned` 动态获取？
- [ ] **禁止组件**：是否没有使用 Material3 Switch / TopAppBar / AlertDialog / ModalBottomSheet / DropdownMenu / Slider / Divider？
- [ ] **圆形按钮**：是否所有圆形按钮都用 `CircleIconButton`？是否没有手写 Box+CircleShape？
- [ ] **列表行按压**：可点击行是否使用 `SettingsNavigateRow` 等标准组件或 `pressDimPublic`？是否没有裸 `Modifier.clickable`？
- [ ] **DrawerStackState**：页面有抽屉时，最外层是否有 `CompositionLocalProvider(LocalDrawerStack provides remember { DrawerStackState() })`？
- [ ] **VIBRATE 权限**：app 的 AndroidManifest.xml 是否已声明？
- [ ] **verticalScroll**：SettingsPage 的 content 内是否**没有**额外的 `verticalScroll`？（不论有无 tabs 都已内置）
