# 特殊组件 — GoldjucXSlider / LottieHeroSection / pressDimPublic

> 滑块、Lottie 动画区域、按压效果等特殊用途组件。

源码路径：`src/main/java/com/miui/goldjucx_ui/SettingsLayout.kt`

## GoldjucXSlider

胶囊滑块组件，支持拖拽、惯性衰减、点击跳转、连续振动反馈。

| 参数 | 类型 | 说明 |
|------|------|------|
| value | Float | 当前值（0.0–1.0） |
| onValueChange | (Float) -> Unit | 值变化回调 |

### 模板代码

```kotlin
@Composable
fun SliderExample() {
    var brightness by remember { mutableFloatStateOf(0.5f) }

    SettingsCard {
        SliderSettingRow(
            title = "模糊强度",
            value = brightness,
            onValueChange = { brightness = it },
            valueLabel = "${(brightness * 100).toInt()}%",
            onReset = { brightness = 0.5f }
        )
    }
}
```

```kotlin
// 单独使用 GoldjucXSlider（不带标题行）
var volume by remember { mutableFloatStateOf(0.7f) }
GoldjucXSlider(value = volume, onValueChange = { volume = it })
```

## LottieHeroSection

Lottie 动画 + 说明文案，用于页面顶部功能介绍区域。

| 参数 | 类型 | 说明 |
|------|------|------|
| animationAsset | String | assets 文件名 |
| title | String | 标题 |
| description | AnnotatedString | 描述（支持蓝色链接） |
| onDescriptionClick | (Int) -> Unit | 链接点击回调 |
| animationHeight | Dp | 动画区高度（默认 280dp） |

### 模板代码

```kotlin
@Composable
fun IntroScreenWithLottie(onBack: () -> Unit) {
    SettingsPage(title = "智能防窥", onBack = onBack) {
        LottieHeroSection(
            animationAsset = "peep_guard_intro.json",
            title = "智能防窥",
            description = buildAnnotatedString {
                append("检测到窥视时自动保护隐私。")
                pushStringAnnotation("link", "learn_more")
                withStyle(SpanStyle(color = GoldjucXColors.primary)) { append("了解更多") }
                pop()
            },
            onDescriptionClick = { offset ->
                // 处理链接点击
            }
        )
        SettingsSectionTitle("设置")
        SettingsCard {
            SettingsSwitchRow(title = "开启防窥", checked = false, onCheckedChange = { })
        }
    }
}
```

```kotlin
// 简单用法（无链接）
LottieHeroSection(
    animationAsset = "shield_animation.json",
    title = "安全中心",
    description = buildAnnotatedString { append("保护你的手机安全") }
)
```

## pressDimPublic

按压效果修饰符：缩放到 0.97 + 6% 黑色覆盖层。

### 模板代码

```kotlin
// 自定义可点击行（标准行组件已内置，通常不需要手动加）
Box(
    Modifier
        .fillMaxWidth()
        .pressDimPublic(onClick = { navigateToDetail() })
        .padding(horizontal = 16.dp, vertical = 14.dp)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_item), null, Modifier.size(24.dp), tint = Color.Unspecified)
        Spacer(Modifier.width(12.dp))
        Text("自定义行内容", style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))
    }
}
```

**注意**：以下组件已内置 pressDim，无需额外添加：
- `SettingsNavigateRow`
- `SelectableOptionRow`
- `CheckableOptionRow`
- `SettingsIconNavigateRow`

## 常见错误

```kotlin
// ❌ 使用 Material3 Slider
Slider(value = v, onValueChange = { v = it })

// ✅ 使用 GoldjucXSlider
GoldjucXSlider(value = v, onValueChange = { v = it })
```

```kotlin
// ❌ 列表行用 Modifier.clickable 没有按压效果
Row(Modifier.fillMaxWidth().clickable { doAction() }) { ... }

// ✅ 用 pressDimPublic
Row(Modifier.fillMaxWidth().pressDimPublic(onClick = { doAction() })) { ... }

// ✅ 或直接用标准行组件（已内置）
SettingsNavigateRow(title = "操作", onClick = { doAction() })
```

```kotlin
// ❌ 给已内置 pressDim 的组件再加一层
SettingsNavigateRow(
    title = "详情",
    onClick = { },
    modifier = Modifier.pressDimPublic(onClick = { })  // 双重效果！
)

// ✅ 标准行组件不需要额外加
SettingsNavigateRow(title = "详情", onClick = { })
```
