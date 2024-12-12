package com.cardinalblue.kraftshade.pipeline.input

import androidx.annotation.CallSuper
import com.cardinalblue.kraftshade.pipeline.Pipeline

abstract class SampledInput<T : Any> : Input<T>() {
    private var lastSample: T? = null
    private var isDirty: Boolean = true

    /**
     * Sample produces new values. This is called internally when the input is dirty
     * and needs to update its value.
     */
    protected abstract fun Pipeline.provideSample(): T

    /**
     * Get the current value. If the input is dirty, it will sample a new value first.
     * This ensures consistent values within the same frame.
     */
    override fun Pipeline.internalGet(): T {
        trackInput(this@SampledInput)
        if (isDirty) {
            lastSample = provideSample()
            isDirty = false
        }
        return lastSample!!
    }

    /**
     * Mark this input as dirty, causing it to sample a new value on the next get() call.
     */
    @CallSuper
    internal open fun markDirty() {
        isDirty = true
    }
}

class WrappedSampledInput<T : Any>(
    private val action: () -> T
) : SampledInput<T>() {
    override fun Pipeline.provideSample(): T = action()
}

fun <T : Any> sampledInput(action: () -> T) : SampledInput<T> {
    return WrappedSampledInput(action)
}
