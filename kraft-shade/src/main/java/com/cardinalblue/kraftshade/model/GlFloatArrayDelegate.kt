package com.cardinalblue.kraftshade.model

import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.util.GlUniformDelegate
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class GlFloatArray(size: Int) {
    val backingArray = FloatArray(size)
    internal var callback: (() -> Unit)? = null

    operator fun get(index: Int): Float = backingArray[index]
    operator fun set(index: Int, value: Float) {
        backingArray[index] = value
        // for the init, for example, using glFloatArrayOf, the callback will be null
        callback?.invoke()
    }
}

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

fun glFloatArrayOf(vararg elements: Float): GlFloatArray = GlFloatArray(elements.size).also {
    elements.forEachIndexed { index, element -> it[index] = element }
}
