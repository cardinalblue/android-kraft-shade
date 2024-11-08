package com.cardinalblue.kraftshade.pipeline.input

import org.junit.Assert.*
import org.junit.Test

class TimeInputTest {
    @Test
    fun `test sample when not started`() {
        var currentTime = 0L
        val timeInput = TimeInput { currentTime }

        assert(timeInput.sample() == 0f)
        currentTime = 100
        assert(timeInput.sample() == 0f)
    }

    @Test
    fun `test sample and get`() {
        var currentTime = 0L
        val timeInput = TimeInput { currentTime }
        timeInput.start()
        assert(timeInput.get() == 0f)
        assert(timeInput.sample() == 0f)

        currentTime = 100
        assertEquals(0f, timeInput.get())
        assertEquals(0.1f, timeInput.sample())
        assertEquals(0.1f, timeInput.get())
    }

    @Test
    fun `test start pause start`() {
        var currentTime = 5000L
        val timeInput = TimeInput { currentTime }
        timeInput.start()
        currentTime = 6000L
        assertEquals(1f, timeInput.sample())
        currentTime = 7000L
        timeInput.pause()
        assertEquals(2f, timeInput.get())
        currentTime = 8000L
        assertEquals(2f, timeInput.sample())
        currentTime = 9000L
        timeInput.start()
        currentTime = 10000L
        assertEquals(3f, timeInput.sample())
    }
}
