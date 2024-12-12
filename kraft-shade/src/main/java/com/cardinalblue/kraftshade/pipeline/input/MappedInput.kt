package com.cardinalblue.kraftshade.pipeline.input

import com.cardinalblue.kraftshade.pipeline.Pipeline

/**
 * Note that this class doesn't provide the sample behavior, if you want to map a sampled input, you
 * should use [MappedSampledInput].
 */
class MappedInput<T : Any, R : Any>(
    private val source: Input<T>,
    private val mapper: (T) -> R,
) : Input<R>() {
    override fun Pipeline.internalGet(): R {
        val sourceValue = with(source) { internalGet() }
        return mapper(sourceValue)
    }
}
