package com.cardinalblue.kraftshade.model

import androidx.annotation.ColorInt

@JvmInline
value class GlColor private constructor(@ColorInt val intValue: Int) {
    val int: Int get() = intValue
    val r: Int get() = intValue shr 16 and 0xFF
    val g: Int get() = intValue shr 8 and 0xFF
    val b: Int get() = intValue and 0xFF
    val a: Int get() = intValue shr 24 and 0xFF

    val rFloat: Float get() = r / 255f
    val gFloat: Float get() = g / 255f
    val bFloat: Float get() = b / 255f
    val aFloat: Float get() = a / 255f

    val vec3: FloatArray get() = floatArrayOf(rFloat, gFloat, bFloat)
    val vec4: FloatArray get() = floatArrayOf(rFloat, gFloat, bFloat, aFloat)

    fun alterAlpha(a: Float) = normalizedRGBA(rFloat, gFloat, bFloat, a)

    companion object {
        fun normalizedRGBA(r: Float, g: Float, b: Float, a: Float = 1f) = GlColor(
            ((a * 255).toInt() shl 24) or
            ((r * 255).toInt() shl 16) or
            ((g * 255).toInt() shl 8) or
            (b * 255).toInt()
        )

        fun int(@ColorInt intValue: Int) = GlColor(intValue)

        // common colors
        val Black get() = normalizedRGBA(0f, 0f, 0f)
        val White get() = normalizedRGBA(1f, 1f, 1f)
        val Red get() = normalizedRGBA(1f, 0f, 0f)
        val Green get() = normalizedRGBA(0f, 1f, 0f)
        val Blue get() = normalizedRGBA(0f, 0f, 1f)
        val Yellow get() = normalizedRGBA(1f, 1f, 0f)
        val Cyan get() = normalizedRGBA(0f, 1f, 1f)
        val Magenta get() = normalizedRGBA(1f, 0f, 1f)
        val Transparent get() = normalizedRGBA(0f, 0f, 0f, 0f)
    }
}
