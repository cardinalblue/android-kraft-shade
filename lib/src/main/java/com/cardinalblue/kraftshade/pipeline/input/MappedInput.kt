package com.cardinalblue.kraftshade.pipeline.input

import kotlin.math.max
import kotlin.math.min

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

fun SampledInput<Float>.scale(scale: Float): SampledInput<Float> = map { it * scale }

fun SampledInput<Float>.bounceBetween(value1: Float, value2: Float): SampledInput<Float> {
    val min = min(value1, value2)
    val max = max(value1, value2)
    val interval = max - min
    return map { value ->
        val intervalValue = value % (interval * 2f)
        if (intervalValue < interval) {
            intervalValue + value1
        } else {
            2f * interval - intervalValue
        }
    }
}
