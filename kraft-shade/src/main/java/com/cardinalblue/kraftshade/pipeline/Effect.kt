package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.shader.buffer.GlBuffer

/**
 * This is a common interface that represents an effect that can be drawn to a [GlBuffer].
 * Before drawTo is called, all the setup should be done in the implementation. This is the last
 * step an actual effect (either Pipeline or KraftShader) should do.
 */
interface Effect {
    suspend fun drawTo(buffer: GlBuffer)
    suspend fun destroy()
}
