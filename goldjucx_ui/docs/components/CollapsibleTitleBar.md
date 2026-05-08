# CollapsibleTitleBar — 折叠标题栏（内部组件）

> ⚠️ **内部组件**：由 `SettingsPage` 自动调用，开发者无需直接使用。
> 仅在不使用 `SettingsPage` 的自定义页面布局中才需手动调用。

源码路径：`src/main/java/com/goldjucx/ui/CollapsibleTitleBar.kt`

## 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String | — | 标题文字 |
| scrollState | ScrollState? | null | 普通滚动状态（与 lazyListState 二选一） |
| lazyListState | LazyListState? | null | 懒加载列表状态 |
| onBack | (() -> Unit)? | null | 返回按钮回调，null = 不显示 |
| showBackButton | Boolean | true | 是否显示返回按钮位 |
| backgroundColor | Color | surfaceLow | 背景色 |
| hazeState | HazeState? | null | 传入即启用毛玻璃 |
| includeStatusBar | Boolean | true | 是否内置状态栏间距 |
| trailingContent | @Composable (() -> Unit)? | null | 右侧自定义内容 |

**返回值**：`Dp` — 当前标题栏动画高度，用于内容区 `padding(top = ...)`。

## 手动搭建模板（仅特殊页面需要）

```kotlin
@Composable
fun CustomPageWithHaze(onBack: () -> Unit) {
    val hazeState = remember { HazeState() }
    val scrollState = rememberScrollState()

    Box(Modifier.fillMaxSize().background(GoldjucXColors.surfaceLow)) {
        val titleBarHeight = CollapsibleTitleBar(
            title = "页面标题",
            scrollState = scrollState,
            onBack = onBack,
            hazeState = hazeState
        )
        Column(
            Modifier.fillMaxSize()
                .hazeSource(state = hazeState)
                .verticalScroll(scrollState)
                .padding(top = titleBarHeight)
        ) {
            SettingsCard { SettingsSwitchRow(title = "开关", checked = false, onCheckedChange = { }) }
        }
    }
}
```

**关键**：Modifier 顺序必须是 `.hazeSource().verticalScroll().padding(top)`，否则毛玻璃无法生效。

## 常见错误

```kotlin
// ❌ Modifier 顺序错误（内容不会滚到标题栏后面）
Column(Modifier.padding(top = h).verticalScroll(scrollState)) { ... }

// ✅ 正确顺序
Column(Modifier.hazeSource(hazeState).verticalScroll(scrollState).padding(top = h)) { ... }
```

```kotlin
// ❌ 外层加 statusBarsPadding（毛玻璃不覆盖状态栏）
Box(Modifier.fillMaxSize().statusBarsPadding()) { CollapsibleTitleBar(...) }

// ✅ 不加（CollapsibleTitleBar 内部自己管理）
Box(Modifier.fillMaxSize()) { CollapsibleTitleBar(...) }
```
