package com.cardinalblue.kraftshade.model

data class GlSize(
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float get() = width.toFloat() / height

    val vec2: FloatArray by lazy { floatArrayOf(width.toFloat(), height.toFloat()) }
}
