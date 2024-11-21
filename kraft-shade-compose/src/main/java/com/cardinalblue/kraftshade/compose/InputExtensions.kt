package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.State
import com.cardinalblue.kraftshade.pipeline.input.SampledInput

fun <T : Any> State<T>.asSampledInput(): SampledInput<T> {
    return object : SampledInput<T>() {
        override fun provideSample(): T {
            return value
        }
    }
}
