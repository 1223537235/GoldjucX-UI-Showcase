# CommonComponents — 通用基础组件

> 开关、圆形按钮、按钮组、分段选择器、列表项等基础组件。

源码路径：`src/main/java/com/miui/goldjucx_ui/CommonComponents.kt`

## 组件总览

| 组件 | 用途 |
|------|------|
| `GoldjucXSwitch` | 统一开关（49×28dp，自带振动） |
| `CircleIconButton` | 统一圆形图标按钮（44dp，8dp阴影，95%白） |
| `PrimaryButton` | 主操作按钮（蓝底白字） |
| `SecondaryButton` | 次级按钮（灰底） |
| `SegmentedTabBar` | 分段选择器 |
| `BackButton` | 返回按钮（CircleIconButton 封装） |
| `AppSwitchRow` | 应用图标+名称+开关 |
| `SelectableAppIcon` | 网格选择应用图标（带勾选） |
| `FeatureListItem` | 图标+标题+副标题功能介绍行 |
| `CodeBlock` | 等宽代码块 |
| `bounceClick()` | 点击缩放 Modifier |

## 模板代码（直接复制）

### GoldjucXSwitch

```kotlin
var isEnabled by remember { mutableStateOf(false) }

GoldjucXSwitch(checked = isEnabled, onCheckedChange = { isEnabled = it })
```

### CircleIconButton

```kotlin
// 返回按钮
CircleIconButton(iconRes = R.drawable.ic_back, contentDescription = "返回", onClick = onBack)

// 关闭按钮
CircleIconButton(iconRes = R.drawable.ic_close, contentDescription = "关闭", onClick = { onClose() })

// 搜索按钮（自定义 tint）
CircleIconButton(iconRes = R.drawable.ic_search, contentDescription = "搜索", onClick = { }, tint = GoldjucXColors.primary)

// 禁用状态
CircleIconButton(iconRes = R.drawable.ic_confirm, contentDescription = "确认", onClick = { }, enabled = false)
```

### PrimaryButton / SecondaryButton

```kotlin
// 标准主按钮（336×50dp，用于页面底部）
PrimaryButton(text = "确认开启", onClick = { enableFeature() })

// 紧凑主按钮（自适应宽度×40dp，用于卡片内部）
PrimaryButton(text = "添加", onClick = { }, compact = true)

// 次级按钮（灰底，fillMaxWidth）
SecondaryButton(text = "暂不开启", onClick = { dismiss() })

// 按钮组合（常见于页面底部）
Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    PrimaryButton(text = "立即开启", onClick = { enable() })
    SecondaryButton(text = "暂不开启", onClick = { dismiss() })
}
```

### SegmentedTabBar

```kotlin
var selectedTab by remember { mutableIntStateOf(0) }

SegmentedTabBar(
    tabs = listOf("全部", "已开启", "已关闭"),
    selectedIndex = selectedTab,
    onTabSelected = { selectedTab = it }
)
```

### AppSwitchRow（应用列表）

```kotlin
SettingsCard {
    appList.forEach { app ->
        AppSwitchRow(
            icon = app.icon,  // ImageBitmap
            label = app.name,
            checked = app.enabled,
            onCheckedChange = { toggleApp(app) }
        )
    }
}
```

### SelectableAppIcon（网格选择）

```kotlin
val gridItems = appList.chunked(4)
gridItems.forEach { row ->
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        row.forEach { app ->
            SelectableAppIcon(
                icon = app.icon,
                label = app.name,
                isSelected = app in selectedApps,
                onToggle = { toggleSelection(app) }
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}
```

### FeatureListItem（功能介绍）

```kotlin
SettingsCard {
    FeatureListItem(
        iconRes = R.drawable.ic_shield,
        title = "智能检测",
        subtitle = "AI 识别窥视行为并自动保护"
    )
    FeatureListItem(
        iconRes = R.drawable.ic_blur,
        title = "画面模糊",
        subtitle = "检测到窥视时屏幕自动模糊"
    )
}
```

### bounceClick / pressDimPublic

```kotlin
// bounceClick：单纯缩放动画（scale 0.9 + 弹性回弹）
Box(Modifier.bounceClick().clickable { doAction() }) { ... }

// pressDimPublic：缩放 0.97 + 6% 黑色覆盖（用于可点击行）
Box(Modifier.pressDimPublic(onClick = { navigate() })) {
    // 自定义行内容
}
```

## 常见错误

```kotlin
// ❌ 手写 Box 实现圆形按钮
Box(Modifier.size(44.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(Color(0xF2FFFFFF))) {
    Icon(painterResource(R.drawable.ic_close), null, Modifier.size(21.dp))
}

// ✅ 统一使用 CircleIconButton
CircleIconButton(iconRes = R.drawable.ic_close, contentDescription = "关闭", onClick = { })
```

```kotlin
// ❌ 使用 Material3 Switch
Switch(checked = isOn, onCheckedChange = { isOn = it })

// ✅ 使用 GoldjucXSwitch
GoldjucXSwitch(checked = isOn, onCheckedChange = { isOn = it })
```

```kotlin
// ❌ 列表行用裸 Modifier.clickable（无按压效果）
Row(Modifier.clickable { navigate() }) { Text("点击") }

// ✅ 用 pressDimPublic 或标准行组件
Row(Modifier.pressDimPublic(onClick = { navigate() })) { Text("点击") }
// 或更好：
SettingsNavigateRow(title = "点击", onClick = { navigate() })
```
