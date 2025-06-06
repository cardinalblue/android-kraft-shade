package com.cardinalblue.kraftshade.shader.util

import android.opengl.GLES30
import com.cardinalblue.kraftshade.model.*
import com.cardinalblue.kraftshade.shader.KraftShader
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class GlUniformDelegate<T : Any>(
    protected val name: String,
    protected val required: Boolean = true,
    protected val checkValueForSet: (T) -> Unit = {},
) : ReadWriteProperty<KraftShader, T> {
    private val location: Int by lazy {
        GLES30.glGetUniformLocation(shader.glProgId, name).also { location ->
            check(!required || location != -1) {
                "required uniform not found: $name"
            }
        }
    }
    private lateinit var shader: KraftShader
    private var value: T? = null
    private var valueHashCode: Int = Int.MAX_VALUE

    private var thisInitialized: Boolean = false

    override fun getValue(thisRef: KraftShader, property: KProperty<*>): T {
        initialize(thisRef)
        shader = thisRef
        return requireNotNull(value) { "value not set"}
    }

    override fun setValue(thisRef: KraftShader, property: KProperty<*>, value: T) {
        checkValueForSet(value)
        initialize(thisRef)
        setValue(value)
        thisRef.updateProperty(name, value)
    }

    private fun initialize(thisRef: KraftShader) {
        if (thisInitialized) return
        shader = thisRef
    }

    private fun setValue(value: T) {
        // Each glUniform* call sends data to the GPU and may incur a synchronization cost. Redundant
        // updates mean you're paying this cost even when nothing changes.
        val hashCode = hash(value)
        if (hashCode == valueHashCode) {
            return
        }

        this.value = value
        this.valueHashCode = hashCode

        shader.runOnDraw(name) {
            val location = location
            if (location == -1)  return@runOnDraw
            when (value) {
                is Boolean -> GLES30.glUniform1i(location, if (value) 1 else 0)
                is Int -> GLES30.glUniform1i(location, value)
                is Float -> GLES30.glUniform1f(location, value)
                is FloatArray -> setFloatArrayValues(value)
                is GlFloatArray -> setFloatArrayValues(value.backingArray)

                is GlSize -> setFloatArrayValues(value.vec2)
                is GlSizeF -> setFloatArrayValues(value.vec2)

                is GlMat2 -> GLES30.glUniformMatrix2fv(location, 1, false, value.arr, 0)
                is GlMat3 -> GLES30.glUniformMatrix3fv(location, 1, false, value.arr, 0)
                is GlMat4 -> GLES30.glUniformMatrix4fv(location, 1, false, value.arr, 0)

                is GlVec2 -> setFloatArrayValues(value.vec2)
                is GlVec3 -> setFloatArrayValues(value.vec3)
                is GlVec4 -> setFloatArrayValues(value.vec4)

                else -> throw IllegalArgumentException("Invalid value type: ${value::class.java}")
            }
        }
    }

    private fun setFloatArrayValues(array: FloatArray) {
        when (array.size) {
            2 -> GLES30.glUniform2fv(location, 1, array, 0)
            3 -> GLES30.glUniform3fv(location, 1, array, 0)
            4 -> GLES30.glUniform4fv(location, 1, array, 0)
            else -> GLES30.glUniform1fv(location, array.size, array, 0)
        }
    }

    private fun hash(value: Any): Int {
        when (value) {
            is Int,
            is Float,
            is Boolean -> return value.hashCode()
        }
        val array: FloatArray = when (value) {
            is FloatArray -> value
            is GlFloatArray -> value.backingArray
            is GlSize -> value.vec2
            is GlSizeF -> value.vec2
            is GlMat -> value.arr
            is GlVec2 -> value.vec2
            is GlVec3 -> value.vec3
            is GlVec4 -> value.vec4
            else -> throw IllegalArgumentException("Invalid value type: ${value::class.java}")
        }

        return array.contentHashCode()
    }
}
