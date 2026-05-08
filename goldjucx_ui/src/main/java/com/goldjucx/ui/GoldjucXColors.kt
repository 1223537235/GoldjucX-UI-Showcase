package com.goldjucx.ui

import androidx.compose.ui.graphics.Color

/**
 * GoldjucX 色彩与阴影体系
 *
 * 可直接复用到任何 GoldjucX 风格项目，只需修改 Primary 色即可。
 *
 * ## 接入方式
 * ```kotlin
 * import com.goldjucx.ui.GoldjucXColors
 * Text("Hello", color = GoldjucXColors.onSurface)
 * ```
 *
 * ## 色彩规范
 * | Token              | 值           | 用途                     |
 * |--------------------|-------------|--------------------------|
 * | primary            | #3482FF     | 主色/开关/链接/选中态      |
 * | surfaceLow         | #F3F3F3     | 页面底色                  |
 * | surfaceContainer   | #FFFFFF     | 卡片/弹窗底色             |
 * | onSurface          | #000000     | 一级文字                  |
 * | onSurfaceSecondary | 80% black   | 二级文字/弹窗正文          |
 * | onSurfaceTertiary  | 60% black   | 三级文字/副标题/说明        |
 * | onSurfaceQuaternary| 40% black   | 四级文字/占位符/辅助信息     |
 * | outline            | 10% black   | 分割线/边框               |
 * | tertiaryContainer  | 5% black    | 次级按钮背景              |
 * | gray               | #8C9DB0     | 分组标题                  |
 *
 * ## 阴影规范
 * | 场景         | elevation | ambientColor   | spotColor      |
 * |-------------|-----------|----------------|----------------|
 * | 返回按钮     | 8dp       | 0x0F000000     | 0x14000000     |
 * | 弹窗菜单     | 30dp      | 0x29000000     | 0x29000000     |
 */
object GoldjucXColors {
    val primary = Color(0xFF3482FF)
    val surfaceLow = Color(0xFFF3F3F3)
    val surfaceContainer = Color(0xFFFFFFFF)
    val onSurface = Color(0xFF000000)
    val onSurfaceSecondary = Color(0xCC000000)
    val onSurfaceTertiary = Color(0x99000000)
    val onSurfaceQuaternary = Color(0x66000000)
    val gray = Color(0xFF8C9DB0)
    val outline = Color(0x1A000000)
    val tertiaryContainer = Color(0x0D000000)
    val danger = Color(0xFFFF3B30)

    // 阴影预设
    val shadowAmbientLight = Color(0x0F000000)
    val shadowSpotLight = Color(0x14000000)
    val shadowAmbientHeavy = Color(0x29000000)
    val shadowSpotHeavy = Color(0x29000000)

    // 常用组合色
    val buttonBackground = Color(0xF2FFFFFF)
    val popupBackground = Color(0xF5FFFFFF)
    val popupBorder = Color(0xFFF9F9F9)
    val scrimColor = Color.Black
    val codeBackground = Color(0xFFF5F5F5)
}
