package com.cardinalblue.kraftshade.shader.util

import com.cardinalblue.kraftshade.model.GlSizeF
import com.cardinalblue.kraftshade.shader.KraftShader
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SerializableField<T: Any>(default: T): ReadWriteProperty<KraftShader, T> {
    private var value: T = default

    override fun getValue(
        thisRef: KraftShader,
        property: KProperty<*>
    ): T {
        return value
    }

    override fun setValue(
        thisRef: KraftShader,
        property: KProperty<*>,
        value: T
    ) {
        thisRef.serializableFields[property.name] = when (value) {
            is GlSizeF -> value.vec2
            else -> error("PreserveField can only be used with GlSize")
        }
        this.value = value
    }
}
