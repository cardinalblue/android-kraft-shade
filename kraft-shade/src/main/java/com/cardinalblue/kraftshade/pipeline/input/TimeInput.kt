package com.cardinalblue.kraftshade.pipeline.input

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

    override fun provideSample(): Float {
        // update lastSample or not
        if (started) {
            lastSampleTime = getTime()
        }

        val diff = lastSampleTime - startTime
        return (pausedTime + diff).seconds()
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
        markDirty()
        get()
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
        markDirty()
        get()
    }

    private fun Long.seconds(): Float {
        return this / 1_000f
    }
}
