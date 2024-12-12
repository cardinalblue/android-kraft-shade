package com.cardinalblue.kraftshade.pipeline.input

import com.cardinalblue.kraftshade.pipeline.Pipeline

/**
 * @param getTime a function to get the current time in milliseconds. this is useful for testing.
 */
class TimeInput(
    private val getTime: () -> Long = { System.currentTimeMillis() }
) : SampledInput<Float>() {
    private var started = false

    private var startTime = 0L
    private var lastSampleTime = 0L

    /**
     * This is the accumulated time when the timer is paused. It's not like [startTime] and
     * [lastSampleTime]. It's an accumulated time, so we don't do subtraction on it.
     */
    private var pausedTime = 0L

    /**
     * internal for testing purpose
     */
    internal fun internalProvideSample(): Float {
        // update lastSample or not
        if (started) {
            lastSampleTime = getTime()
        }

        val diff = lastSampleTime - startTime
        return (pausedTime + diff).seconds()
    }

    override fun Pipeline.provideSample(): Float {
        return internalProvideSample()
    }

    fun reset() {
        started = false
        startTime = 0L
        lastSampleTime = 0L
        pausedTime = 0L
    }

    fun start() {
        if (started) return

        started = true
        startTime = getTime()
        lastSampleTime = startTime
        // to update the last sample
        internalProvideSample()
    }

    /**
     * effectively pause
     */
    fun pause() {
        if (!started) return
        started = false
        // do the sampling now
        lastSampleTime = getTime()
        pausedTime += (lastSampleTime - startTime)
        startTime = lastSampleTime
        internalProvideSample()
    }

    private fun Long.seconds(): Float {
        return this / 1_000f
    }
}
