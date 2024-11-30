package com.cardinalblue.kraftshade.model

data class GlSizeF(
    val width: Float,
    val height: Float,
) {
    val aspectRatio: Float get() = width / height
    val area: Float get() = width * height

    val vec2: FloatArray by lazy { floatArrayOf(width, height) }

    companion object {
        val Unit = GlSizeF(1f, 1f)
    }
}