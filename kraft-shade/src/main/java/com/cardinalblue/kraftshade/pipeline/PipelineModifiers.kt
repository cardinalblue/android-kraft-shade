package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.dsl.SerialTextureInputPipelineScope
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

abstract class PipelineModifierWithInputTexture {
    abstract suspend fun GraphPipelineSetupScope.addStep(
        inputTexture: TextureProvider,
        outputBuffer: GlBufferProvider,
    )

    suspend fun SerialTextureInputPipelineScope.addStep() {
        graphStep { inputTexture ->
            addStep(inputTexture, graphTargetBuffer)
        }
    }
}

abstract class PipelineModifierWithoutInputTexture {
    abstract suspend fun GraphPipelineSetupScope.addStep(outputBuffer: GlBufferProvider)

    // ignoring the input texture
    suspend fun SerialTextureInputPipelineScope.addStep() {
        graphStep {
            addStep(graphTargetBuffer)
        }
    }
}
