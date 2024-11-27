package com.cardinalblue.kraftshade.pipeline.input

fun <T : Any, R : Any> Input<T>.map(mapper: (T) -> R): Input<R> {
    // these two methods might be confusing for the users, so we do the check here to prevent
    // sampled input being wrapped by MappedInput which looses the sampling behavior.
    if (this is SampledInput) {
        return MappedSampledInput(this, mapper)
    }
    return MappedInput(this, mapper)
}
