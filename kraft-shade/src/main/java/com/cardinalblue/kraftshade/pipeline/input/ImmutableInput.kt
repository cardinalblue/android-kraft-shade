package com.cardinalblue.kraftshade.pipeline.input

class ImmutableInput<T : Any>(
    private val value: T
) : Input<T> {
    override fun get(): T = value
}

fun <T : Any> constInput(value: T) = ImmutableInput(value)
