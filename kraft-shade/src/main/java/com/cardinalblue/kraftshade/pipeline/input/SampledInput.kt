package com.cardinalblue.kraftshade.pipeline.input

import androidx.annotation.CallSuper

abstract class SampledInput<T : Any> : Input<T> {
    private var lastSample: T? = null
    private var isDirty: Boolean = true

    /**
     * Sample produces new values. This is called internally when the input is dirty
     * and needs to update its value.
     */
    protected abstract fun provideSample(): T

    /**
     * Get the current value. If the input is dirty, it will sample a new value first.
     * This ensures consistent values within the same frame.
     */
    override fun get(): T {
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

internal class WrappedSampledInput<T : Any>(
    private val action: () -> T
) : SampledInput<T>() {
    override fun provideSample(): T = action()
}

fun <T : Any> sampledInput(action: () -> T) : SampledInput<T> {
    return WrappedSampledInput(action)
}
