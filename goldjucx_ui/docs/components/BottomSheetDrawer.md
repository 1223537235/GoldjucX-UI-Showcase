# BottomSheetDrawer — 底部抽屉

> 支持多层叠加、背景模糊、磨砂玻璃、下拉关闭、标题栏的底部抽屉组件。

源码路径：`src/main/java/com/goldjucx/ui/BottomSheetDrawer.kt`

## SheetBackground

```kotlin
sealed class SheetBackground {
    data object Gray : SheetBackground()                              // 灰色实色（默认）
    data object White : SheetBackground()                             // 白色实色
    data class Blur(val blurRadius: Dp = 40.dp) : SheetBackground()  // 磨砂玻璃
}
```

## 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| onClose | () -> Unit | — | 关闭回调 |
| showHandle | Boolean | true | 拖拽手柄 |
| background | SheetBackground | Gray | 背景样式 |
| heightFraction | Float | 0.93 | 最大高度占比 |
| headerIcon | @DrawableRes Int? | ic_back | 左上角图标，null 隐藏 |
| title | String? | null | 标题 |
| subtitle | String? | null | 副标题 |
| blurScrim | Boolean | true | 蒙层模糊 |
| dismissOnPullDown | Boolean | true | 下拉关闭 |

## 模板代码

### 模板 1：单层抽屉

```kotlin
CompositionLocalProvider(LocalDrawerStack provides remember { DrawerStackState() }) {
    Box(Modifier.fillMaxSize()) {
        SettingsPage(title = "标题", onBack = onBack) { _ ->
            SettingsCard { SettingsNavigateRow(title = "打开抽屉", onClick = { showSheet = true }) }
        }
        if (showSheet) {
            BottomSheetDrawer(onClose = { showSheet = false }, title = "抽屉标题", headerIcon = R.drawable.ic_close) { animateOut ->
                val scope = rememberCoroutineScope()
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    SettingsCard { SettingsNavigateRow(title = "内容", onClick = { }) }
                    PrimaryButton(text = "完成", onClick = { scope.launch { animateOut(); showSheet = false } })
                }
            }
        }
    }
}
```

### 模板 2：磨砂玻璃

```kotlin
BottomSheetDrawer(
    onClose = { showSheet = false },
    background = SheetBackground.Blur(blurRadius = 40.dp)
) { animateOut -> ... }
```

## 关键规则

- **`animateOut` 必须使用**：关闭前必须 `scope.launch { animateOut(); show = false }`
- **抽屉层平级**：多个抽屉在同一个 Box 内平级，不嵌套
- **DrawerStackState**：最外层需提供 `CompositionLocalProvider(LocalDrawerStack provides ...)`
- **statusBarsPadding 外**：抽屉不能放在 statusBarsPadding 内部（蒙层会被裁切）

## 常见错误

```kotlin
// ❌ 旧参数（已废弃）
BottomSheetDrawer(sheetColor = Color.White, blurSheet = true, ...)

// ✅ 新参数
BottomSheetDrawer(background = SheetBackground.Blur(), ...)
```

```kotlin
// ❌ 忽略 animateOut
BottomSheetDrawer(...) { _ -> Button(onClick = { show = false }) }

// ✅ 先动画再关闭
BottomSheetDrawer(...) { animateOut ->
    val scope = rememberCoroutineScope()
    Button(onClick = { scope.launch { animateOut(); show = false } })
}
```
