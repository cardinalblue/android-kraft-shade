package com.cardinalblue.kraftshade.pipeline.input

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Input<T : Any> : ReadOnlyProperty<Any?, T> {
    fun get(): T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }
}
