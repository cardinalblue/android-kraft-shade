package com.cardinalblue.kraftshade.model

import kotlin.math.max
import kotlin.math.min

data class GlSizeF(
    val width: Float,
    val height: Float,
) {
    constructor(size: Float) : this(size, size)

    val aspectRatio: Float get() = width / height
    val area: Float get() = width * height

    val vec2: FloatArray by lazy { floatArrayOf(width, height) }

    val major: Float = max(width, height)
    val minor: Float = min(width, height)

    operator fun times(scale: Float): GlSizeF = GlSizeF(width * scale, height * scale)

    companion object {
        val Unit = GlSizeF(1f, 1f)
    }
}