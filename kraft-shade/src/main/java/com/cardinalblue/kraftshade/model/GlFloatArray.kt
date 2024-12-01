package com.cardinalblue.kraftshade.model

import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A wrapper class for FloatArray that provides OpenGL-specific functionality.
 * This class allows for automatic updates to shader uniforms when array values are modified.
 *
 * @property backingArray The underlying FloatArray that stores the actual values
 * @property callback An optional callback that is triggered when values in the array are modified
 * @param size The size of the float array to create
 */
class GlFloatArray(size: Int) {
    val backingArray = FloatArray(size)
    internal var callback: (() -> Unit)? = null

    /**
     * Gets the value at the specified index in the array.
     * @param index The position to retrieve the value from
     * @return The float value at the specified index
     */
    operator fun get(index: Int): Float = backingArray[index]

    /**
     * Sets the value at the specified index in the array and triggers the callback if one is set.
     * @param index The position to set the value at
     * @param value The float value to set
     */
    operator fun set(index: Int, value: Float) {
        backingArray[index] = value
        // for the init, for example, using glFloatArrayOf, the callback will be null
        callback?.invoke()
    }
}

/**
 * A property delegate for managing OpenGL float array uniforms in shaders.
 * This delegate handles the synchronization between shader uniforms and their corresponding [GlFloatArray] values.
 *
 * @property name The name of the uniform in the shader
 * @property required Whether this uniform is required to be present in the shader
 * @property checkValueForSet A validation function that is called before setting the value
 */
class GlFloatArrayDelegate(
    name: String,
    required: Boolean = true,
    checkValueForSet: (GlFloatArray) -> Unit = {},
) : ReadWriteProperty<KraftShader, GlFloatArray> {
    private val backingDelegate = GlUniformDelegate(name, required, checkValueForSet)
    private var shaderReference: WeakReference<KraftShader> = WeakReference(null)

    private val glFloatArrayCallback = {
        val shader = checkNotNull(shaderReference.get()) { "delegate should be used first" }
        val array = backingDelegate.getValue(shader, GlFloatArrayDelegate::backingDelegate)
        backingDelegate.setValue(shader, GlFloatArrayDelegate::backingDelegate, array)
    }

    override fun getValue(thisRef: KraftShader, property: KProperty<*>): GlFloatArray {
        shaderReference = WeakReference(thisRef)
        return backingDelegate.getValue(thisRef, GlFloatArrayDelegate::backingDelegate)
    }

    override fun setValue(thisRef: KraftShader, property: KProperty<*>, value: GlFloatArray) {
        shaderReference = WeakReference(thisRef)
        value.callback = glFloatArrayCallback
        backingDelegate.setValue(thisRef, property, value)
    }
}

/**
 * Creates a new [GlFloatArray] initialized with the specified elements.
 * This is a convenience function similar to [floatArrayOf] but for [GlFloatArray].
 *
 * @param elements The float values to initialize the array with
 * @return A new [GlFloatArray] containing the specified elements
 */
fun glFloatArrayOf(vararg elements: Float): GlFloatArray = GlFloatArray(elements.size).also {
    elements.forEachIndexed { index, element -> it[index] = element }
}
