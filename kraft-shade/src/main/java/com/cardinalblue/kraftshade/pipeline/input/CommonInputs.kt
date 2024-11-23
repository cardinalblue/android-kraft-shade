package com.cardinalblue.kraftshade.pipeline.input

object CommonInputs {
    fun time(start: Boolean = true) = TimeInput().apply {
        if (start) start()
    }
}
