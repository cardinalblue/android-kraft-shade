package com.cardinalblue.kraftshade.shader.util

import android.opengl.GLES20
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
        GLES20.glGetUniformLocation(shader.glProgId, name).also { location ->
            check(!required || location != -1) {
                "required uniform not found: $name"
            }
        }
    }
    private lateinit var shader: KraftShader
    private lateinit var value: T

    private var thisUpdated: Boolean = false

    override fun getValue(thisRef: KraftShader, property: KProperty<*>): T {
        updateThis(thisRef)
        shader = thisRef
        return value
    }

    override fun setValue(thisRef: KraftShader, property: KProperty<*>, value: T) {
        checkValueForSet(value)
        updateThis(thisRef)
        setValue(value)
    }

    private fun updateThis(thisRef: KraftShader) {
        if (thisUpdated)  return
        shader = thisRef
    }

    private fun setValue(value: T) {
        this.value = value
        shader.runOnDraw(name) {
            val location = location
            if (location == -1)  return@runOnDraw
            when (value) {
                is Int -> GLES20.glUniform1i(location, value)
                is Float -> GLES20.glUniform1f(location, value)
                is FloatArray -> setFloatArrayValues(value)
                is GlFloatArray -> setFloatArrayValues(value.backingArray)

                is GlSize -> GLES20.glUniform2fv(location, 1, value.vec2, 0)
                is GlSizeF -> GLES20.glUniform2fv(location, 1, value.vec2, 0)

                is GlMat2 -> GLES20.glUniformMatrix2fv(location, 1, false, value.arr, 0)
                is GlMat3 -> GLES20.glUniformMatrix3fv(location, 1, false, value.arr, 0)
                is GlMat4 -> GLES20.glUniformMatrix4fv(location, 1, false, value.arr, 0)

                is GlVec2 -> GLES20.glUniform2fv(location, 1, value.vec2, 0)
                is GlVec3 -> GLES20.glUniform3fv(location, 1, value.vec3, 0)
                is GlVec4 -> GLES20.glUniform4fv(location, 1, value.vec4, 0)

                else -> throw IllegalArgumentException("Invalid value type: ${value::class.java}")
            }
        }
    }

    private fun setFloatArrayValues(array: FloatArray) {
        when (array.size) {
            2 -> GLES20.glUniform2fv(location, 1, array, 0)
            3 -> GLES20.glUniform3fv(location, 1, array, 0)
            4 -> GLES20.glUniform4fv(location, 1, array, 0)
            else -> GLES20.glUniform1fv(location, array.size, array, 0)
        }
    }
}