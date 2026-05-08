package com.goldjucx.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

object MiHapticHelper {

    private const val TAG = "MiHapticHelper"

    private val hapticVersion: String by lazy {
        try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            method.invoke(null, "sys.haptic.version", "1.0") as String
        } catch (e: Exception) {
            "1.0"
        }
    }

    val isHaptic2Supported: Boolean get() = hapticVersion == "2.0"

    private val hapticUtilClass: Class<*>? by lazy {
        try {
            Class.forName("miui.util.HapticFeedbackUtil")
        } catch (e: Exception) {
            null
        }
    }

    private var cachedInstance: Any? = null
    private var instanceInitAttempted = false

    private fun getInstance(context: Context): Any? {
        if (instanceInitAttempted) return cachedInstance
        instanceInitAttempted = true
        val clazz = hapticUtilClass ?: return null
        cachedInstance = tryCreateInstance(clazz, context)
        return cachedInstance
    }

    private fun tryCreateInstance(clazz: Class<*>, context: Context): Any? {
        try {
            val ctor = clazz.getDeclaredConstructor(Context::class.java, Boolean::class.javaPrimitiveType)
            ctor.isAccessible = true
            return ctor.newInstance(context, true)
        } catch (_: Exception) {}

        try {
            val ctor = clazz.getDeclaredConstructor(Context::class.java)
            ctor.isAccessible = true
            return ctor.newInstance(context)
        } catch (_: Exception) {}

        try {
            val ctor = clazz.getDeclaredConstructor()
            ctor.isAccessible = true
            return ctor.newInstance()
        } catch (_: Exception) {}

        return null
    }

    fun performHaptic(context: Context, view: View?, id: Int): Boolean {
        val clazz = hapticUtilClass
        val instance = getInstance(context)
        if (clazz != null && instance != null) {
            try {
                val method = clazz.getMethod("performExtHapticFeedback", Int::class.javaPrimitiveType)
                val result = method.invoke(instance, id) as? Boolean ?: false
                if (result) return true
            } catch (e: Exception) {
                Log.d(TAG, "performExtHapticFeedback($id) failed: ${e.message}")
            }
        }

        if (view != null) {
            try {
                val result = view.performHapticFeedback(id)
                if (result) return true
            } catch (_: Exception) {}
        }
        return false
    }
}

object GoldjucXHaptic {
    const val SWITCH = 9
    const val BUTTON_LARGE = 2
    const val BUTTON_MIDDLE = 3
    const val BUTTON_SMALL = 4
    const val GEAR_LIGHT = 5
    const val GEAR_HEAVY = 6
    const val BOUNDARY = 0
    const val TAB_SLIDE = 204

    fun perform(context: Context, view: View?, effectId: Int) {
        if (MiHapticHelper.isHaptic2Supported) {
            MiHapticHelper.performHaptic(context, view, effectId)
        } else {
            val (duration, amplitude) = fallbackParams(effectId)
            val vibrator = getVibrator(context)
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        }
    }

    private fun fallbackParams(effectId: Int): Pair<Long, Int> = when (effectId) {
        SWITCH -> 8L to 30
        BUTTON_LARGE -> 10L to 40
        BUTTON_MIDDLE -> 8L to 25
        BUTTON_SMALL -> 6L to 20
        GEAR_LIGHT -> 6L to 10
        GEAR_HEAVY -> 8L to 20
        BOUNDARY -> 12L to 50
        TAB_SLIDE -> 8L to 20
        else -> 8L to 25
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}

@Composable
fun rememberHaptic(): (Int) -> Unit {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view) { { effectId: Int -> GoldjucXHaptic.perform(context, view, effectId) } }
}
