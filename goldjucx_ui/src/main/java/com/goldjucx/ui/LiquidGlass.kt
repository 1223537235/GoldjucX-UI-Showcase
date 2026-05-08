package com.goldjucx.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kotlin.math.cos
import kotlin.math.sin

/**
 * 液态玻璃材质参数。
 *
 * 仅封装光学属性（模糊、折射、高光、饱和度等），与形状（宽高、圆角、位置）解耦。
 * 形状由调用方通过 [LiquidGlass] 的 modifier 和 cornerRadius 参数控制。
 *
 * 预设：
 * - [Thin]     — 薄玻璃（低模糊 + 高透明，iOS 浅色导航栏风格）
 * - [Regular]  — 标准玻璃（通用场景）
 * - [Thick]    — 厚毛玻璃（高模糊 + 高 tint，iOS 卡片底衬风格）
 * - [Crystal]  — 水晶玻璃（低模糊 + 强折射 + 高饱和，iOS 26 Liquid Glass 风格）
 */
data class LiquidGlassMaterial(
    val blurRadius: Float = 40f,
    val saturation: Float = 1.9f,
    val tintStrength: Float = 0.12f,
    val refractionStrength: Float = 35f,
    val lightAngleDeg: Float = 225f,
    val specularIntensity: Float = 0.5f,
    val specularWidth: Float = 13f,
    val specularSharpness: Float = 6f,
    val backlightIntensity: Float = 0.15f,
    val brightnessBoost: Float = 1.03f,
) {
    companion object {
        val Thin = LiquidGlassMaterial(
            blurRadius = 24f, tintStrength = 0.06f, saturation = 1.5f,
            refractionStrength = 25f, specularIntensity = 0.4f
        )

        val Regular = LiquidGlassMaterial(
            blurRadius = 40f
        )

        val Thick = LiquidGlassMaterial(
            blurRadius = 64f, tintStrength = 0.24f, saturation = 1.6f,
            refractionStrength = 40f
        )

        val Crystal = LiquidGlassMaterial(
            blurRadius = 16f, tintStrength = 0.04f, saturation = 2.2f,
            refractionStrength = 55f, specularIntensity = 0.85f,
            specularSharpness = 10f, brightnessBoost = 1.05f
        )
    }
}

object LiquidGlassDefaults {
    var material by androidx.compose.runtime.mutableStateOf(LiquidGlassMaterial.Crystal)

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        val dps = context.createDeviceProtectedStorageContext()
        prefs = dps.getSharedPreferences("liquid_glass_defaults", android.content.Context.MODE_PRIVATE)
        prefs?.let { sp ->
            if (sp.contains("blurRadius")) {
                val loaded = LiquidGlassMaterial(
                    blurRadius = sp.getFloat("blurRadius", 16f),
                    saturation = sp.getFloat("saturation", 2.2f),
                    tintStrength = sp.getFloat("tintStrength", 0.04f),
                    refractionStrength = sp.getFloat("refractionStrength", 55f),
                    lightAngleDeg = sp.getFloat("lightAngleDeg", 225f),
                    specularIntensity = sp.getFloat("specularIntensity", 0.85f),
                    specularWidth = sp.getFloat("specularWidth", 13f),
                    specularSharpness = sp.getFloat("specularSharpness", 10f),
                    backlightIntensity = sp.getFloat("backlightIntensity", 0.15f),
                    brightnessBoost = sp.getFloat("brightnessBoost", 1.05f)
                )
                material = loaded
                android.util.Log.d("LiquidGlass", "init loaded: blur=${loaded.blurRadius} refr=${loaded.refractionStrength}")
            } else {
                android.util.Log.d("LiquidGlass", "init: no saved data, using Crystal defaults")
            }
        }
    }

    fun applyAndSave(m: LiquidGlassMaterial) {
        material = m
        val editor = prefs?.edit() ?: run {
            android.util.Log.e("LiquidGlass", "applyAndSave: prefs is null!")
            return
        }
        editor.putFloat("blurRadius", m.blurRadius)
        editor.putFloat("saturation", m.saturation)
        editor.putFloat("tintStrength", m.tintStrength)
        editor.putFloat("refractionStrength", m.refractionStrength)
        editor.putFloat("lightAngleDeg", m.lightAngleDeg)
        editor.putFloat("specularIntensity", m.specularIntensity)
        editor.putFloat("specularWidth", m.specularWidth)
        editor.putFloat("specularSharpness", m.specularSharpness)
        editor.putFloat("backlightIntensity", m.backlightIntensity)
        editor.putFloat("brightnessBoost", m.brightnessBoost)
        val ok = editor.commit()
        android.util.Log.d("LiquidGlass", "applyAndSave: commit=$ok blur=${m.blurRadius} refr=${m.refractionStrength}")
    }
}

interface LiquidGlassSceneScope : BoxScope {
    fun registerGlass(id: Any, rect: Rect, cornerRadiusPx: Float)
    fun unregisterGlass(id: Any)
}

@Composable
fun LiquidGlassScene(
    modifier: Modifier = Modifier,
    material: LiquidGlassMaterial = LiquidGlassMaterial.Regular,
    content: @Composable LiquidGlassSceneScope.() -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Box(modifier) {
            val fallback = object : LiquidGlassSceneScope, BoxScope by this {
                override fun registerGlass(id: Any, rect: Rect, cornerRadiusPx: Float) = Unit
                override fun unregisterGlass(id: Any) = Unit
            }
            fallback.content()
        }
        return
    }
    LiquidGlassSceneImpl(modifier, material, content)
}

@Composable
fun LiquidGlassSceneScope.LiquidGlass(
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    val cornerPx = with(density) { cornerRadius.toPx() }
    val id = remember { Any() }

    DisposableEffect(id) {
        onDispose { unregisterGlass(id) }
    }

    LaunchedEffect(cornerPx) {}

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val size = coords.size
                registerGlass(
                    id = id,
                    rect = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height),
                    cornerRadiusPx = cornerPx
                )
            },
        content = content
    )
}

// ─────────────────────────────────────────────────────────────
// 私有实现
// ─────────────────────────────────────────────────────────────

private data class GlassInfo(val rect: Rect, val cornerRadiusPx: Float)

private class LiquidGlassSceneState {
    var sceneSize by mutableStateOf(IntSize.Zero)
    var sceneOffsetX by mutableFloatStateOf(0f)
    var sceneOffsetY by mutableFloatStateOf(0f)
    val glasses = mutableStateMapOf<Any, GlassInfo>()
    var currentMaterial by mutableStateOf(LiquidGlassMaterial())
}

private class LiquidGlassSceneScopeImpl(
    boxScope: BoxScope,
    private val state: LiquidGlassSceneState
) : LiquidGlassSceneScope, BoxScope by boxScope {
    override fun registerGlass(id: Any, rect: Rect, cornerRadiusPx: Float) {
        val localRect = Rect(
            rect.left - state.sceneOffsetX,
            rect.top - state.sceneOffsetY,
            rect.right - state.sceneOffsetX,
            rect.bottom - state.sceneOffsetY
        )
        state.glasses[id] = GlassInfo(localRect, cornerRadiusPx)
    }
    override fun unregisterGlass(id: Any) {
        state.glasses.remove(id)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LiquidGlassSceneImpl(
    modifier: Modifier,
    material: LiquidGlassMaterial,
    content: @Composable LiquidGlassSceneScope.() -> Unit
) {
    val state = remember { LiquidGlassSceneState() }
    val shader = remember { RuntimeShader(LIQUID_GLASS_SHADER_SRC) }
    state.currentMaterial = material

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                state.sceneOffsetX = pos.x
                state.sceneOffsetY = pos.y
                state.sceneSize = coords.size
            }
            .graphicsLayer {
                val sz = state.sceneSize
                val glass = state.glasses.values.firstOrNull()
                val mat = state.currentMaterial
                if (sz.width > 0 && sz.height > 0 && glass != null) {
                    shader.setFloatUniform(
                        "resolution", sz.width.toFloat(), sz.height.toFloat()
                    )
                    shader.setFloatUniform(
                        "glassRect",
                        glass.rect.left, glass.rect.top,
                        glass.rect.right, glass.rect.bottom
                    )
                    shader.setFloatUniform("cornerRadius", glass.cornerRadiusPx)
                    mat.applyToShader(shader)

                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "content")
                        .asComposeRenderEffect()
                } else {
                    renderEffect = null
                }
            }
    ) {
        val scope = remember(state) { LiquidGlassSceneScopeImpl(this, state) }
        scope.content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun LiquidGlassMaterial.applyToShader(shader: RuntimeShader) {
    val angleRad = lightAngleDeg * Math.PI.toFloat() / 180f
    shader.setFloatUniform("refractionStrength", refractionStrength)
    shader.setFloatUniform("lightDir", cos(angleRad), sin(angleRad))
    shader.setFloatUniform("specularIntensity", specularIntensity)
    shader.setFloatUniform("backlightIntensity", backlightIntensity)
    shader.setFloatUniform("specularWidth", specularWidth)
    shader.setFloatUniform("specularSharpness", specularSharpness)
    shader.setFloatUniform("brightnessBoost", brightnessBoost)
    shader.setFloatUniform("blurRadius", blurRadius)
    shader.setFloatUniform("saturation", saturation)
    shader.setFloatUniform("tintStrength", tintStrength)
}

private const val LIQUID_GLASS_SHADER_SRC = """
uniform shader content;
uniform float2 resolution;
uniform float4 glassRect;
uniform float cornerRadius;
uniform float refractionStrength;
uniform float2 lightDir;
uniform float specularIntensity;
uniform float backlightIntensity;
uniform float specularWidth;
uniform float specularSharpness;
uniform float brightnessBoost;
uniform float blurRadius;
uniform float saturation;
uniform float tintStrength;

float sdRoundedBox(float2 p, float2 center, float2 halfSize, float r) {
    float2 q = abs(p - center) - halfSize + float2(r, r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

half4 blurSample(float2 c, float r) {
    if (r < 0.5) {
        return content.eval(c);
    }
    half4 s = content.eval(c) * 0.0744;
    float TWO_PI = 6.28318530718;

    float ri = r * 0.3;
    for (int i = 0; i < 6; i++) {
        float a = float(i) * (TWO_PI / 6.0);
        s += content.eval(c + float2(cos(a), sin(a)) * ri) * 0.058;
    }

    float rm = r * 0.6;
    for (int i = 0; i < 12; i++) {
        float a = float(i) * (TWO_PI / 12.0) + (TWO_PI / 24.0);
        s += content.eval(c + float2(cos(a), sin(a)) * rm) * 0.030;
    }

    float ro = r;
    for (int i = 0; i < 18; i++) {
        float a = float(i) * (TWO_PI / 18.0);
        s += content.eval(c + float2(cos(a), sin(a)) * ro) * 0.012;
    }

    return s;
}

half3 adjustSaturation(half3 color, float sat) {
    float luma = dot(color, half3(0.2126, 0.7152, 0.0722));
    return half3(luma) + (color - half3(luma)) * sat;
}

half4 main(float2 fragCoord) {
    float2 center = (glassRect.xy + glassRect.zw) * 0.5;
    float2 halfSize = (glassRect.zw - glassRect.xy) * 0.5;
    float d = sdRoundedBox(fragCoord, center, halfSize, cornerRadius);

    if (d > 0.5) {
        return content.eval(fragCoord);
    }

    if (d > -1.0) {
        half4 src = content.eval(fragCoord);
        float shadowFactor = smoothstep(0.5, -1.0, d) * 0.25;
        return half4(src.rgb * (1.0 - shadowFactor), src.a);
    }

    float edgeDist = -d;
    float specEdge = 1.0 - smoothstep(0.0, max(specularWidth, 1.0), edgeDist);
    float edgeInfluence = 1.0 - smoothstep(0.0, max(cornerRadius, 1.0), edgeDist);

    float2 toCenter = center - fragCoord;
    float lenToCenter = length(toCenter) + 0.0001;
    float2 outwardNormal = -toCenter / lenToCenter;

    float refractFactor = edgeInfluence * edgeInfluence;
    float2 sampleOffset = outwardNormal * refractFactor * refractionStrength;
    float2 sampleCoord = clamp(fragCoord + sampleOffset, float2(0.0), resolution);

    half4 color = blurSample(sampleCoord, blurRadius);
    color.rgb = adjustSaturation(color.rgb, saturation);
    color.rgb = mix(color.rgb, half3(1.0), tintStrength);

    float specular = max(dot(outwardNormal, lightDir), 0.0);
    specular = pow(specular, specularSharpness) * specEdge;
    color.rgb += half3(specular * specularIntensity);

    float backlight = max(dot(outwardNormal, -lightDir), 0.0);
    backlight = pow(backlight, specularSharpness * 2.0) * specEdge;
    color.rgb += half3(backlight * backlightIntensity);

    color.rgb = color.rgb * brightnessBoost;
    return color;
}
"""
