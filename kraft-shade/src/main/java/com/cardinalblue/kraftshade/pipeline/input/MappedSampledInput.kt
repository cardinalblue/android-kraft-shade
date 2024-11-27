package com.cardinalblue.kraftshade.pipeline.input

import kotlin.math.max
import kotlin.math.min

class MappedSampledInput<I : Any, O : Any>(
    private val source: SampledInput<I>,
    private val mapping: (I) -> O,
) : SampledInput<O>() {
    override fun provideSample(): O {
        return mapping(source.get())
    }

    override fun markDirty() {
        super.markDirty()
        source.markDirty()
    }
}

fun Input<Float>.scale(scale: Float): Input<Float> = map { it * scale }

fun Input<Float>.bounceBetween(value1: Float, value2: Float): Input<Float> {
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
