package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.shader.buffer.GlBuffer

/**
 * This is a common interface that represents an effect that can be drawn to a [GlBuffer].
 * Before drawTo is called, all the setup should be done in the implementation. This is the last
 * step an actual effect KraftShader should do.
 */
interface EffectExecution {
    /**
     * Execute the effect. Make sure the setup is done before calling this.
     */
    suspend fun run()

    suspend fun destroy()
}
