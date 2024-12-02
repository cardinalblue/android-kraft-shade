package com.cardinalblue.kraftshade.model

open class GlVec3(
    x: Float,
    y: Float,
    z: Float,
) {
    val vec3: FloatArray = floatArrayOf(x, y, z)

    var x: Float get() = vec3[0]
        set(value) {
            vec3[0] = value
        }

    var y: Float get() = vec3[1]
        set(value) {
            vec3[1] = value
        }

    var z: Float get() = vec3[2]
        set(value) {
            vec3[2] = value
        }

    var r: Float get() = vec3[0]
        set(value) {
            vec3[0] = value
        }

    var g: Float get() = vec3[1]
        set(value) {
            vec3[1] = value
        }

    var b: Float get() = vec3[2]
        set(value) {
            vec3[2] = value
        }

    constructor(value: Float) : this(value, value, value)

    fun copy(
        x: Float = this.x,
        y: Float = this.y,
        z: Float = this.z,
    ): GlVec3 = GlVec3(x, y, z)

    companion object {
        val Zero = GlVec3(0f)
        val One = GlVec3(1f)
    }
}
