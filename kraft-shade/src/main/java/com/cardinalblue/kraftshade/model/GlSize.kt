package com.cardinalblue.kraftshade.model

import android.graphics.Bitmap

data class GlSize(
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float get() = width.toFloat() / height
    val area: Int get() = width * height

    val vec2: FloatArray by lazy { floatArrayOf(width.toFloat(), height.toFloat()) }

    fun toGlSizeF(): GlSizeF {
        return GlSizeF(width.toFloat(), height.toFloat())
    }

    companion object {
        val ZERO = GlSize(0, 0)
    }
}

val Bitmap.glSize: GlSize get() = GlSize(width, height)
