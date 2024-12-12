package com.cardinalblue.kraftshade.pipeline.input

import com.cardinalblue.kraftshade.pipeline.Pipeline

abstract class Input<T : Any> {
    /**
     * We make it internal only because we want to know all the sampled inputs in the configuration
     * phase, so all the sampled inputs can be marked as dirty at the start of the frame.
     */
    internal abstract fun Pipeline.internalGet(): T
}
