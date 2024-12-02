package com.cardinalblue.kraftshade.model

open class GlVec2(
    x: Float,
    y: Float,
) {
    val vec2: FloatArray = floatArrayOf(x, y)

    var x: Float get() = vec2[0]
        set(value) {
            vec2[0] = value
        }

    var y: Float get() = vec2[1]
        set(value) {
            vec2[1] = value
        }

    constructor(value: Float) : this(value, value)

    companion object {
        val Zero = GlVec2(0f)
        val One = GlVec2(1f)
    }
}
