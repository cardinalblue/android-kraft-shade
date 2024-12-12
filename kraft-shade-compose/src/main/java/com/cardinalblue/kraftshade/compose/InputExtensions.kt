package com.cardinalblue.kraftshade.compose

import androidx.compose.runtime.State
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.pipeline.input.WrappedSampledInput

fun <T : Any> State<T>.asSampledInput(): SampledInput<T> {
    return WrappedSampledInput(action = { value })
}
