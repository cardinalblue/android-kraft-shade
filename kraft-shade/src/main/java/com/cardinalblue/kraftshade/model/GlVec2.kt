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

    operator fun get(index: Int): Float = vec2[index]

    operator fun set(index: Int, value: Float) {
        vec2[index] = value
    }

    fun copy(
        x: Float = this.x,
        y: Float = this.y,
    ): GlVec2 = GlVec2(x, y)

    companion object {
        val Zero = GlVec2(0f)
        val One = GlVec2(1f)
    }
}
