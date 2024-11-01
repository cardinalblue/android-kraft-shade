package com.cardinalblue.kraftshade.pipeline.input

class ConstInput<T : Any>(
    private val value: T
) : Input<T> {
    override fun get(): T = value
}

fun <T : Any> constInput(value: T) = ConstInput(value)
