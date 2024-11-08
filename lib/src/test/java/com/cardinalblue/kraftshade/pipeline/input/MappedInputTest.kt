package com.cardinalblue.kraftshade.pipeline.input

import org.junit.Assert
import org.junit.Test

class MappedInputTest {
    @Test
    fun `mapping as double`() {
        var testValue = 2

        val source = sampledInput { testValue }

        val doubledInput = source.map { it * 2f }

        Assert.assertEquals(4f, doubledInput.sample())

        testValue = 7
        Assert.assertEquals(4f, doubledInput.get())
        Assert.assertEquals(14f, doubledInput.sample())
    }
}