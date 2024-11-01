package com.cardinalblue.kraftshade.pipeline.input

abstract class SampledInput<T : Any> : Input<T> {
    private var lastSample: T? = null

    /**
     * Sample produces new values, and to make sure values set to each shader is consistent for a
     * specific frame, the sample of an [SampledInput] should be called at the beginning of the
     * execution of a pipeline.
     */
    abstract fun provideSample(): T

    /**
     * Get doesn't produce new values, so if it's connected to multiple shaders in the pipeline,
     * they are getting the same value using get.
     */
    override fun get(): T {
        return lastSample ?: sample()
    }

    fun sample(): T {
        val value = provideSample()
        lastSample = value
        return value
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
