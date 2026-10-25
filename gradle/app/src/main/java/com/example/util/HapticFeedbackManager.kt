package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf

enum class HapticFeedbackType {
    KEY_TAP,
    OPERATOR_TAP,
    CALCULATION_SUCCESS,
    ERROR_ALERT,
    TICK,
    NONE
}

class HapticFeedbackManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val hasVibrator: Boolean = vibrator?.hasVibrator() == true

    fun perform(type: HapticFeedbackType) {
        when (type) {
            HapticFeedbackType.KEY_TAP -> performKeyTap()
            HapticFeedbackType.OPERATOR_TAP -> performOperatorTap()
            HapticFeedbackType.CALCULATION_SUCCESS -> performCalculationSuccess()
            HapticFeedbackType.ERROR_ALERT -> performErrorAlert()
            HapticFeedbackType.TICK -> performTick()
            HapticFeedbackType.NONE -> {}
        }
    }

    /**
     * Subtle, crisp vibration for standard numeric key taps.
     */
    fun performKeyTap() {
        if (!hasVibrator) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile sensation for operators (+, -, *, /) and function keys.
     */
    fun performOperatorTap() {
        if (!hasVibrator) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(22, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(22)
            }
        } catch (_: Exception) {}
    }

    /**
     * Rewarding, firm vibration when calculation equals (=) is evaluated.
     */
    fun performCalculationSuccess() {
        if (!hasVibrator) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 20, 30, 35),
                        intArrayOf(0, 180, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    /**
     * Error or syntax warning vibration pattern (two rapid pulses).
     */
    fun performErrorAlert() {
        if (!hasVibrator) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 30), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 30, 40, 30), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Light tick when toggling modes, switching sections, or clearing (AC).
     */
    fun performTick() {
        if (!hasVibrator) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                vibrator?.vibrate(VibrationEffect.createOneShot(8, 90))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(8)
            }
        } catch (_: Exception) {}
    }
}

val LocalHapticManager = staticCompositionLocalOf<HapticFeedbackManager?> { null }
