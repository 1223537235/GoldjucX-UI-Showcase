# SettingsLayout — 设置页布局组件集

> 设置页面标准行组件、卡片容器、页面脚手架等。

源码路径：`src/main/java/com/goldjucx/ui/SettingsLayout.kt`

## SettingsPage 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String? | null | 页面标题，null = 不显示标题栏 |
| onBack | (() -> Unit)? | null | 返回按钮回调，null = 不显示 |
| trailingContent | @Composable (() -> Unit)? | null | 标题栏右侧按钮 |
| tabs | List\<GlassTab\>? | null | Tab 栏（2-5 个），null = 普通单页 |
| tabBarStyle | HazeStyle? | null | Tab 栏材质，null = 内置默认 |
| backgroundColor | Color | surfaceLow | 页面背景色 |
| content | ColumnScope.(page: Int) -> Unit | — | page 为当前 Tab 索引，无 Tab 时固定为 0 |

**GlassTab**：`data class GlassTab(val label: String, @DrawableRes val iconRes: Int)`

**内置能力**（不需要手动加）：statusBarsPadding、CollapsibleTitleBar + 毛玻璃、verticalScroll、GlassBottomBar（有 tabs 时）、底部 100dp 留白、滚动边界震动。

## 组件总览

| 组件 | 用途 |
|------|------|
| `SettingsCard` | 白色圆角卡片（20dp 圆角，外边距 12dp horizontal, 10dp top） |
| `SettingsSwitchRow` | 标题+副标题+开关（70dp 高） |
| `SettingsNavigateRow` | 标题+值+箭头（自带 pressDim） |
| `SettingsValueRow` | 标题+副标题+值+上下箭头 |
| `SettingsActionRow` | 标题+自定义右侧内容 |
| `SettingsSectionTitle` | 分组小标题（灰色 13sp） |
| `SettingsHintText` | 行间说明文字（40% black） |
| `SettingsHeroSection` | 图文介绍区（标题+描述） |
| `SettingsIconSwitchRow` | 图标+标题+开关 |
| `SettingsIconNavigateRow` | 图标+标题+箭头 |
| `ContinuousCardList` | 连续卡片列表（首尾圆角） |
| `SwitchCollapseSection` | 开关联动收起区域 |
| `SliderSettingRow` | 滑块设置行（带重置） |

## 模板代码

### 模板 1：基本设置页

```kotlin
@Composable
fun BasicSettingsScreen(onBack: () -> Unit) {
    var enableFeature by remember { mutableStateOf(true) }

    SettingsPage(title = "功能设置", onBack = onBack) { _ ->
        SettingsSectionTitle("基本设置")
        SettingsCard {
            SettingsSwitchRow(title = "开启功能", subtitle = "开启后将自动运行", checked = enableFeature, onCheckedChange = { enableFeature = it })
            SettingsNavigateRow(title = "灵敏度", value = "中", onClick = { })
        }
        SettingsHintText("开启后会使用前置摄像头检测周围环境")
    }
}
```

### 模板 2：多 Tab 页面

```kotlin
@Composable
fun MultiTabScreen(onBack: () -> Unit) {
    SettingsPage(
        title = "实验室",
        onBack = onBack,
        tabs = listOf(GlassTab("功能", R.drawable.ic_feature), GlassTab("调试", R.drawable.ic_debug))
    ) { page ->
        when (page) {
            0 -> { SettingsSectionTitle("功能"); SettingsCard { SettingsNavigateRow(title = "防窥", onClick = { }) } }
            1 -> { SettingsSectionTitle("调试"); SettingsCard { SettingsNavigateRow(title = "日志", onClick = { }) } }
        }
    }
}
```

### 模板 3：开关联动折叠

```kotlin
SwitchCollapseSection(title = "智能化", checked = smartEnabled, onCheckedChange = { smartEnabled = it }) {
    SettingsCard {
        SettingsNavigateRow(title = "策略管理", onClick = { })
    }
}
```

## 常见错误

```kotlin
// ❌ content 内加 verticalScroll（已内置，会冲突）
SettingsPage(...) { _ -> Column(Modifier.verticalScroll(rememberScrollState())) { ... } }

// ❌ 使用已废弃的 TabbedSettingsPage
TabbedSettingsPage(title = "标题", tabs = ...) { page -> ... }

// ✅ 统一使用 SettingsPage + tabs 参数
SettingsPage(title = "标题", tabs = ...) { page -> ... }
```
