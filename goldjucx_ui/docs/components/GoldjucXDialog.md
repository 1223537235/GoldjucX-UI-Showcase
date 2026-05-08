# GoldjucXDialog — 底部对话框

> 底部滑入式对话框，支持纯文本、自定义内容、多按钮+图片三种变体。已内置 BackHandler。

源码路径：`src/main/java/com/miui/goldjucx_ui/GoldjucXDialog.kt`

## 参数

### 变体一：标准对话框（title + message）

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String | — | 标题 |
| message | String | — | 正文 |
| confirmText | String | "确定" | 确认按钮文字 |
| dismissText | String? | "取消" | 取消按钮文字（null 不显示） |
| onConfirm | () -> Unit | — | 确认回调 |
| onDismiss | () -> Unit | — | 关闭回调（蒙层点击也触发） |

### 变体二：自定义内容

用 `content: @Composable () -> Unit` 替代 message 参数。

### 变体三：多按钮+图片

| 参数 | 类型 | 说明 |
|------|------|------|
| title | String | 标题 |
| message | String | 描述 |
| imageRes | Int? | 顶部图片 |
| buttons | List\<DialogButton\> | 按钮列表（最多4个，竖排） |
| onDismiss | () -> Unit | 关闭回调 |

```kotlin
data class DialogButton(val text: String, val isPrimary: Boolean = false, val onClick: () -> Unit)
```

## 模板代码（直接复制）

### 模板 1：确认/取消对话框

```kotlin
@Composable
fun ScreenWithDialog(onBack: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        // ━━ 内容层 ━━
        SettingsPage(title = "设置", onBack = onBack) {
            SettingsCard {
                SettingsNavigateRow(title = "清除数据", onClick = { showDialog = true })
            }
        }
        // ━━ 对话框层（与内容层平级）━━
        if (showDialog) {
            GoldjucXDialog(
                title = "确认清除",
                message = "清除后将丢失所有本地配置，此操作不可撤销。",
                confirmText = "确定清除",
                dismissText = "取消",
                onConfirm = { clearData(); showDialog = false },
                onDismiss = { showDialog = false }
            )
        }
    }
}
```

### 模板 2：仅确认（无取消按钮）

```kotlin
if (showDialog) {
    GoldjucXDialog(
        title = "更新完成",
        message = "防窥规则已更新到最新版本。",
        confirmText = "我知道了",
        dismissText = null,  // 不显示取消按钮
        onConfirm = { showDialog = false },
        onDismiss = { showDialog = false }
    )
}
```

### 模板 3：自定义内容对话框

```kotlin
if (showDialog) {
    GoldjucXDialog(
        title = "选择模式",
        onConfirm = { showDialog = false },
        onDismiss = { showDialog = false },
        content = {
            Column {
                SelectableOptionRow(title = "标准模式", selected = mode == "standard", onClick = { mode = "standard" })
                SelectableOptionRow(title = "增强模式", selected = mode == "enhanced", onClick = { mode = "enhanced" })
            }
        }
    )
}
```

### 模板 4：多按钮+图片

```kotlin
if (showDialog) {
    GoldjucXDialog(
        title = "检测到风险",
        message = "有人正在窥视你的屏幕",
        imageRes = R.drawable.img_warning,
        buttons = listOf(
            DialogButton("立即锁屏", isPrimary = true) { lockScreen(); showDialog = false },
            DialogButton("模糊屏幕") { blurScreen(); showDialog = false },
            DialogButton("忽略") { showDialog = false }
        ),
        onDismiss = { showDialog = false }
    )
}
```

## 常见错误

```kotlin
// ❌ 使用 Material3 AlertDialog
AlertDialog(onDismissRequest = { ... }, confirmButton = { ... })

// ✅ 使用 GoldjucXDialog（底部滑入式）
GoldjucXDialog(title = "...", message = "...", onConfirm = { ... }, onDismiss = { ... })
```

```kotlin
// ❌ 对话框放在 statusBarsPadding 内（蒙层被裁切）
Box(Modifier.statusBarsPadding()) {
    if (show) GoldjucXDialog(...)
}

// ✅ 对话框在最外层 Box 内与内容层平级
Box(Modifier.fillMaxSize()) {
    SettingsPage(...) { ... }
    if (show) GoldjucXDialog(...)
}
```

```kotlin
// ❌ onConfirm 里忘记改状态（对话框不消失）
onConfirm = { doAction() }  // 没有 showDialog = false！

// ✅ 必须改状态
onConfirm = { doAction(); showDialog = false }
```
