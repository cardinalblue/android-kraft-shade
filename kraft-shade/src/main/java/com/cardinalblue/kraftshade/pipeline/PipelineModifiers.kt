package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.dsl.KraftShadeDsl
import com.cardinalblue.kraftshade.dsl.SerialTextureInputPipelineScope
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

abstract class PipelineModifierWithInputTexture {
    @KraftShadeDsl
    abstract suspend fun GraphPipelineSetupScope.addStep(
        inputTexture: TextureProvider,
        outputBuffer: GlBufferProvider,
    )

    @KraftShadeDsl
    suspend fun SerialTextureInputPipelineScope.addStep() {
        graphStep { inputTexture ->
            addStep(inputTexture, graphTargetBuffer)
        }
    }

    fun asEffectExecutionProvider(
        inputTexture: TextureProvider,
    ): EffectExecutionProvider = EffectExecutionProvider { targetBuffer ->
        pipeline(targetBuffer.provideBuffer()) {
            graphSteps(targetBuffer) {
                addStep(inputTexture, graphTargetBuffer)
            }
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

    fun asEffectExecutionProvider(
    ): EffectExecutionProvider = EffectExecutionProvider { targetBuffer ->
        pipeline(targetBuffer.provideBuffer()) {
            graphSteps(targetBuffer) {
                addStep(graphTargetBuffer)
            }
        }
    }
}
