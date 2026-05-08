# GoldjucX_UI 组件库更新记录

## 2026-05-07

### SettingsPage（设置页脚手架）

- **合并 TabbedSettingsPage**：不再需要两个组件，统一使用 `SettingsPage`，通过 `tabs` 参数控制是否显示底部 Tab 栏
- **API 扁平化重构**：去掉 `TitleBarConfig` 嵌套对象，改为扁平参数 + null 表示不启用
- **内置毛玻璃标题栏**：haze 效果全部内置，调用方零配置自动生效
- **删除 TabbedSettingsPage.kt**：已废弃，功能合并至 SettingsPage

最终 API：
```kotlin
SettingsPage(
    title: String? = null,              // 页面标题，null = 不显示标题栏
    onBack: (() -> Unit)? = null,       // 返回按钮，null = 不显示
    trailingContent: ...? = null,       // 标题栏右侧按钮
    tabs: List<GlassTab>? = null,       // Tab 栏（2-5个 GlassTab），null = 普通单页
    tabBarStyle: HazeStyle? = null,     // Tab 栏材质，null = 内置默认毛玻璃
    backgroundColor: Color = 浅灰,
    content: ColumnScope.(page: Int) -> Unit  // page 为当前页索引，无 Tab 时固定为 0
)
```

### CollapsibleTitleBar（折叠标题栏）

- **内置 statusBarsPadding**：标题栏自己管理状态栏间距，调用方不需要在外层加
- **内置 Haze 毛玻璃**：接收 hazeState 后自动启用渐进式模糊，参数不对外暴露
- **渐进式模糊效果**：顶部 90% 区域接近实色，底部 10% 柔和过渡为模糊带
- **折叠态延伸区**：折叠时底部多出 24dp 空间，给渐进模糊提供过渡区域
- **返回按钮可选**：`onBack = null` 时不显示返回按钮
- 该组件为内部组件，通过 SettingsPage 自动调用，开发者无需直接使用

### BottomSheetDrawer（底部抽屉）

- **移除所有 Haze 相关代码**：haze 不适合有入场动画的覆盖面，已清理
- **API 简化**：用 `SheetBackground` sealed class 替代旧的 sheetColor/blurSheet/blurBackground 参数
  - `SheetBackground.Gray` — 灰色实色（默认）
  - `SheetBackground.White` — 白色实色
  - `SheetBackground.Blur(blurRadius)` — 磨砂玻璃（截图模糊）

### AnchoredPopupMenu（近手弹窗）

- **新增 AnchoredPopupMenuBox**：自包含高层 API，内置锚点定位，在任何上下文（页面、抽屉）中都能正确弹出
- **智能方向**：锚点偏低向上弹出，偏高向下弹出
- **Dialog 层级渲染**：蒙层覆盖状态栏和导航栏，自定义动画蒙层
- **Inset 定位**：弹窗重叠 10dp 进入锚点 padding 区域，紧贴文字
