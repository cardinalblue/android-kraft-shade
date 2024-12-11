package com.cardinalblue.kraftshade.model

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

data class GlSize(
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float get() = width.toFloat() / height
    val area: Int get() = width * height

    val vec2: FloatArray by lazy { floatArrayOf(width.toFloat(), height.toFloat()) }

    val major: Int = max(width, height)
    val minor: Int = min(width, height)

    fun toGlSizeF(): GlSizeF {
        return GlSizeF(width.toFloat(), height.toFloat())
    }

    operator fun times(scale: Int): GlSize = GlSize(width * scale, height * scale)
    operator fun times(scale: Float): GlSizeF = GlSizeF(width * scale, height * scale)

    companion object {
        val ZERO = GlSize(0, 0)
    }
}

val Bitmap.glSize: GlSize get() = GlSize(width, height)
