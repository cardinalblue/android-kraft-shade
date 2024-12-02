package com.cardinalblue.kraftshade.model

open class GlVec4(
    x: Float,
    y: Float,
    z: Float,
    w: Float,
) {
    val vec4: FloatArray = floatArrayOf(x, y, z, w)

    var x: Float get() = vec4[0]
        set(value) {
            vec4[0] = value
        }

    var y: Float get() = vec4[1]
        set(value) {
            vec4[1] = value
        }

    var z: Float get() = vec4[2]
        set(value) {
            vec4[2] = value
        }

    var w: Float get() = vec4[3]
        set(value) {
            vec4[3] = value
        }

    var r: Float get() = vec4[0]
        set(value) {
            vec4[0] = value
        }

    var g: Float get() = vec4[1]
        set(value) {
            vec4[1] = value
        }

    var b: Float get() = vec4[2]
        set(value) {
            vec4[2] = value
        }

    var a: Float get() = vec4[3]
        set(value) {
            vec4[3] = value
        }

    constructor(value: Float) : this(value, value, value, value)

    fun copy(
        x: Float = this.x,
        y: Float = this.y,
        z: Float = this.z,
        w: Float = this.w,
    ): GlVec4 = GlVec4(x, y, z, w)

    companion object {
        val Zero = GlVec4(0f)
        val One = GlVec4(1f)
    }
}
