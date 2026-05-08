# GoldjucXColors — 色彩与阴影体系

> 全局色彩 Token。所有颜色必须从此处取值，禁止凭记忆写 `Color(0x...)`。

源码路径：`src/main/java/com/miui/goldjucx_ui/GoldjucXColors.kt`

## 色值查表（复制粘贴用）

### 文字颜色

| 用途 | Token | 写法 |
|------|-------|------|
| 一级标题/正文 | onSurface | `color = GoldjucXColors.onSurface` |
| 二级文字/弹窗正文 | onSurfaceSecondary | `color = GoldjucXColors.onSurfaceSecondary` |
| 三级文字/副标题 | onSurfaceTertiary | `color = GoldjucXColors.onSurfaceTertiary` |
| 四级文字/占位符 | onSurfaceQuaternary | `color = GoldjucXColors.onSurfaceQuaternary` |
| 分组标题 | gray | `color = GoldjucXColors.gray` |
| 主色/链接/选中文字 | primary | `color = GoldjucXColors.primary` |

### 背景颜色

| 用途 | Token | 写法 |
|------|-------|------|
| 页面底色 | surfaceLow | `Modifier.background(GoldjucXColors.surfaceLow)` |
| 卡片/弹窗底色 | surfaceContainer | `Modifier.background(GoldjucXColors.surfaceContainer)` |
| 次级按钮背景 | tertiaryContainer | `Modifier.background(GoldjucXColors.tertiaryContainer)` |
| 圆形按钮背景 | buttonBackground | `Modifier.background(GoldjucXColors.buttonBackground)` |
| 弹窗菜单背景 | popupBackground | `Modifier.background(GoldjucXColors.popupBackground)` |
| 代码块背景 | codeBackground | `Modifier.background(GoldjucXColors.codeBackground)` |

### 边框/分割

| 用途 | Token | 写法 |
|------|-------|------|
| 分割线/边框 | outline | `Modifier.border(1.dp, GoldjucXColors.outline)` |
| 弹窗描边 | popupBorder | `Modifier.border(1.dp, GoldjucXColors.popupBorder)` |

### 阴影

| 场景 | 写法（直接复制） |
|------|------|
| 圆形按钮 | `.shadow(8.dp, CircleShape, ambientColor = GoldjucXColors.shadowAmbientLight, spotColor = GoldjucXColors.shadowSpotLight)` |
| 弹窗菜单 | `.shadow(40.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x29000000), spotColor = Color(0x29000000))` |
| 底部导航栏 | `.shadow(24.dp, RoundedCornerShape(36.dp), ambientColor = Color(0x40000000), spotColor = Color(0x30000000))` |

## 模板代码（直接复制）

### 文字样式

```kotlin
// 一级标题
Text("标题", style = TextStyle(fontSize = 17.sp, color = GoldjucXColors.onSurface))

// 二级文字
Text("说明", style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceSecondary))

// 三级副标题
Text("副标题", style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceTertiary))

// 四级占位符
Text("请输入", style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.onSurfaceQuaternary))

// 主色链接
Text("了解更多", style = TextStyle(fontSize = 14.sp, color = GoldjucXColors.primary))
```

### 容器背景

```kotlin
// 页面背景
Box(Modifier.fillMaxSize().background(GoldjucXColors.surfaceLow))

// 卡片背景
Box(Modifier.clip(RoundedCornerShape(20.dp)).background(GoldjucXColors.surfaceContainer))
```

### 阴影（圆形按钮已封装，以下用于自定义场景）

```kotlin
// 标准圆形阴影
Modifier
    .shadow(
        elevation = 8.dp,
        shape = CircleShape,
        ambientColor = GoldjucXColors.shadowAmbientLight,
        spotColor = GoldjucXColors.shadowSpotLight
    )
```

## 常见错误

```kotlin
// ❌ 凭记忆写近似色值
Text("标题", color = Color(0xFF333333))
Box(Modifier.background(Color(0xFFF5F5F5)))
Modifier.shadow(8.dp, CircleShape, ambientColor = Color(0x1A000000))

// ✅ 从 GoldjucXColors 取 Token
Text("标题", color = GoldjucXColors.onSurface)
Box(Modifier.background(GoldjucXColors.surfaceLow))
Modifier.shadow(8.dp, CircleShape, ambientColor = GoldjucXColors.shadowAmbientLight, spotColor = GoldjucXColors.shadowSpotLight)
```

```kotlin
// ❌ 用 Color.Gray / Color.LightGray 等系统颜色
Text("说明", color = Color.Gray)

// ✅ 用对应层级的 Token
Text("说明", color = GoldjucXColors.onSurfaceTertiary)
```
