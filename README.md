



# GoldjucX-UI-Showcase

GoldjucX_UI 组件库交互式展示应用。Clone 后直接编译运行，即可体验所有组件效果。

https://github.com/user-attachments/assets/5ad53a2e-4703-4c49-b655-032e12450dec


## 快速开始

```bash
git clone <repository-url>
cd GoldjucX-UI-Showcase
./gradlew installDebug
```

## 项目结构

```
├── app/              展示 App（启动即进入组件展示）
├── goldjucx_ui/      组件库源码 + 文档
└── README.md
```

## Demo 页面

| 页面 | 展示内容 |
|------|---------|
| 页面骨架 | SettingsPage 配置（标题栏、返回、搜索、Tab、背景色） |
| 卡片组件 | 所有 Settings*Row 行组件 + 卡片容器组合 |
| 抽屉 | BottomSheetDrawer 多层堆叠 + 材质切换 |
| 对话框 | GoldjucXDialog 各种变体（按钮布局、内容类型、选择列表） |
| 通用组件 | CircleIconButton、PrimaryButton、SecondaryButton、Switch、Slider、SegmentedTabBar |
| 材质预览 | 卡片/抽屉材质（灰/白/磨砂/Haze/液态玻璃） |

## 组件文档

详见 [goldjucx_ui/docs/](goldjucx_ui/docs/)

## 技术栈

- Kotlin 2.1.20
- Jetpack Compose (BOM 2025.01.01)
- Navigation Compose
- Haze 1.7.2（毛玻璃效果）
- Lottie Compose 6.4.0（动画）
- minSdk 26 / targetSdk 36
