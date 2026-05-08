# AnchoredPopupMenu — 近手弹窗菜单

> 从锚点附近弹出的选择菜单，智能方向判断，Dialog 层级渲染。

源码路径：`src/main/java/com/goldjucx/ui/AnchoredPopupMenu.kt`

## AnchoredPopupMenuBox（推荐）

自包含高层 API，内置锚点定位，无需手动获取坐标。

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| options | List\<String\> | — | 选项列表 |
| selected | String | — | 当前选中项 |
| expanded | Boolean | — | 是否展开 |
| onSelect | (String) -> Unit | — | 选择回调 |
| onDismiss | () -> Unit | — | 关闭回调 |
| checkIconRes | Int | ic_check | 勾选图标 |
| anchor | @Composable () -> Unit | — | 锚点内容 |

智能方向：锚点偏低向上弹，偏高向下弹。弹窗重叠 10dp 紧贴锚点。

## 模板代码

### 模板 1：行内触发

```kotlin
var expanded by remember { mutableStateOf(false) }
var mode by remember { mutableStateOf("标准") }

AnchoredPopupMenuBox(
    options = listOf("标准", "增强", "极致"),
    selected = mode,
    expanded = expanded,
    onSelect = { mode = it; expanded = false },
    onDismiss = { expanded = false }
) {
    SettingsValueRow(title = "防窥模式", value = mode, onClick = { expanded = true })
}
```

### 模板 2：标题栏右上角

```kotlin
SettingsPage(
    title = "标题",
    onBack = onBack,
    trailingContent = {
        AnchoredPopupMenuBox(options = ..., selected = ..., expanded = showMenu, onSelect = { ... }, onDismiss = { showMenu = false }) {
            CircleIconButton(iconRes = R.drawable.ic_more, contentDescription = "更多", onClick = { showMenu = true })
        }
    }
) { _ -> ... }
```

## AnchoredPopupMenu（底层 API）

需手动通过 `onGloballyPositioned` 获取 anchorRight/anchorBottom 坐标。一般直接用 `AnchoredPopupMenuBox`。

## 常见错误

```kotlin
// ❌ 使用 Material3 DropdownMenu
DropdownMenu(expanded = show, ...) { ... }

// ✅ 使用 AnchoredPopupMenuBox
AnchoredPopupMenuBox(expanded = show, ...) { ... }
```
