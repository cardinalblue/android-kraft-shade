package com.cardinalblue.kraftshade.pipeline.input

/**
 * Note that this class doesn't provide the sample behavior, if you want to map a sampled input, you
 * should use [MappedSampledInput].
 */
class MappedInput<T : Any, R : Any>(
    private val source: Input<T>,
    private val mapper: (T) -> R,
) : Input<R> {
    override fun get(): R {
        return mapper(source.get())
    }
}
