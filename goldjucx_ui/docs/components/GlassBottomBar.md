# GlassBottomBar — 玻璃底部导航栏

> 磨砂玻璃风格底部 Tab 栏，支持手势拖动指示器和弹性吸附。

源码路径：`src/main/java/com/miui/goldjucx_ui/GlassBottomBar.kt`

## 数据类

```kotlin
data class GlassTab(val label: String, @DrawableRes val iconRes: Int)
```

## 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| tabs | List\<GlassTab\> | — | Tab 列表（不可为空） |
| selectedTab | Int | — | 当前选中索引 |
| onTabSelected | (Int) -> Unit | — | 切换回调 |
| modifier | Modifier | — | 外部修饰符 |

## 模板代码（直接复制）

### 模板 1：独立使用（不配合 TabbedSettingsPage）

```kotlin
@Composable
fun ScreenWithBottomBar(onBack: () -> Unit) {
    var currentTab by remember { mutableIntStateOf(0) }

    Box(
        Modifier.fillMaxSize()
            .background(GoldjucXColors.surfaceLow)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ━━ 内容区 ━━
        when (currentTab) {
            0 -> HomeContent()
            1 -> SettingsContent()
            2 -> ProfileContent()
        }

        // ━━ 底栏（父 Box 居中）━━
        GlassBottomBar(
            tabs = listOf(
                GlassTab("首页", R.drawable.ic_home),
                GlassTab("设置", R.drawable.ic_settings),
                GlassTab("我的", R.drawable.ic_profile)
            ),
            selectedTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

### 模板 2：配合 TabbedSettingsPage（通常不需要手动写）

TabbedSettingsPage 内部已集成 GlassBottomBar，一般直接用 TabbedSettingsPage 即可。只有在需要完全自定义页面结构时才手动组合。

```kotlin
@Composable
fun CustomTabbedScreen(onBack: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val selectedTab by remember { derivedStateOf { pagerState.currentPage } }

    Box(
        Modifier.fillMaxSize()
            .background(GoldjucXColors.surfaceLow)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            // 每页内容
        }
        GlassBottomBar(
            tabs = listOf(
                GlassTab("Tab1", R.drawable.ic_tab1),
                GlassTab("Tab2", R.drawable.ic_tab2),
                GlassTab("Tab3", R.drawable.ic_tab3)
            ),
            selectedTab = selectedTab,
            onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

## 常见错误

```kotlin
// ❌ 给 GlassBottomBar 加 fillMaxWidth（宽度不对，偏移）
GlassBottomBar(..., modifier = Modifier.fillMaxWidth())

// ✅ 用父 Box 的 Alignment.BottomCenter 居中
Box(Modifier.fillMaxSize()) {
    GlassBottomBar(..., modifier = Modifier.align(Alignment.BottomCenter))
}
```

```kotlin
// ❌ 手动设置底栏 padding（已内置 bottom 16dp）
GlassBottomBar(..., modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))

// ✅ 只设置 align，padding 已内置
GlassBottomBar(..., modifier = Modifier.align(Alignment.BottomCenter))
```
