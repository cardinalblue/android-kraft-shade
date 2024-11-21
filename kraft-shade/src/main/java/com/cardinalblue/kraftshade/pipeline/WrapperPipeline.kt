package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.shader.KraftShader

internal class WrapperPipeline(
    glEnv: GlEnv,
    private val shader: KraftShader
) : Pipeline(glEnv) {
    override suspend fun GlEnv.execute() {
        val targetBuffer = requireNotNull(targetBuffer)
        shader.drawTo(targetBuffer)
    }

    override suspend fun GlEnv.destroy() {
        shader.destroy()
    }
}