package com.cardinalblue.kraftshade.pipeline.input

class MappedInput<I : Any, O : Any>(
    private val source: SampledInput<I>,
    private val mapping: (I) -> O,
) : SampledInput<O>() {
    override fun provideSample(): O {
        return mapping(source.sample())
    }
}

fun <I : Any, O : Any> SampledInput<I>.map(mapping: (I) -> O): SampledInput<O> {
    return MappedInput(this, mapping)
}
