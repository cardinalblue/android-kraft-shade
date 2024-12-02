package com.cardinalblue.kraftshade.model

import android.graphics.Color
import androidx.annotation.ColorInt

class GlColor(
    r: Float,
    g: Float,
    b: Float,
    a: Float = 1f,
) : GlVec4(r, g, b, a) {
    val vec3: FloatArray get() = floatArrayOf(r, g, b)

    val int: Int get() = Color.argb(
        (a * 255).toInt(),
        (r * 255).toInt(),
        (g * 255).toInt(),
        (b * 255).toInt()
    )

    constructor(@ColorInt intValue: Int) : this(
        r = Color.red(intValue) / 255f,
        g = Color.green(intValue) / 255f,
        b = Color.blue(intValue) / 255f,
        a = Color.alpha(intValue) / 255f,
    )

    /**
     * Implementation is the same, but we change the name here, so it won't be confusing.
     */
    fun copyColor(
        r: Float = this.r,
        g: Float = this.g,
        b: Float = this.b,
        a: Float = this.a,
    ): GlColor = GlColor(r, g, b, a)

    companion object {
        // common colors
        val Black get() = GlColor(0f, 0f, 0f)
        val White get() = GlColor(1f, 1f, 1f)
        val Red get() = GlColor(1f, 0f, 0f)
        val Green get() = GlColor(0f, 1f, 0f)
        val Blue get() = GlColor(0f, 0f, 1f)
        val Yellow get() = GlColor(1f, 1f, 0f)
        val Cyan get() = GlColor(0f, 1f, 1f)
        val Magenta get() = GlColor(1f, 0f, 1f)
        val Transparent get() = GlColor(0f, 0f, 0f, 0f)
    }
}

fun GlVec4.asGlColor(): GlColor = GlColor(r, g, b, a)
