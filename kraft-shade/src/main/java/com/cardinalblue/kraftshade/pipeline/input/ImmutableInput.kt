package com.cardinalblue.kraftshade.pipeline.input

import com.cardinalblue.kraftshade.pipeline.Pipeline

class ImmutableInput<T : Any>(
    private val value: T
) : Input<T>() {
    override fun Pipeline.internalGet(): T {
        return value
    }
}

fun <T : Any> constInput(value: T) = ImmutableInput(value)
