package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider

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

    suspend fun onBufferSizeChanged(size: GlSize)
}

fun <S : KraftShader> S.asEffectExecution(
    vararg inputs: Input<*>,
    targetBuffer: GlBufferProvider,
    setup: suspend S.(Array<out Input<*>>) -> Unit = {},
) = object : EffectExecution {
    override suspend fun run() {
        val sampledInputs = inputs
            .filterIsInstance<SampledInput<*>>()
        sampledInputs.forEach { it.markDirty() }
        sampledInputs.forEach { it.get() }
        this@asEffectExecution.setup(inputs)
        drawTo(targetBuffer.provideBuffer())
    }

    override suspend fun destroy() {
        this@asEffectExecution.destroy()
    }

    override suspend fun onBufferSizeChanged(size: GlSize) {
        // no need to do anything since the shader is not aware of the buffer size
    }
}

typealias EffectExecutionProvider = suspend GlEnvDslScope.(glBuffer: GlBuffer) -> EffectExecution

typealias PipelineModifier = suspend GraphPipelineSetupScope.() -> Unit

fun PipelineModifier.asEffectExecutionProvider(): EffectExecutionProvider = { targetBuffer ->
    pipeline(targetBuffer.provideBuffer()) {
        graphSteps(targetBuffer) {
            this@asEffectExecutionProvider()
        }
    }
}

fun createEffectExecutionProviderWithPipeline(
    pipelineModifier: PipelineModifier
): EffectExecutionProvider {
    return pipelineModifier.asEffectExecutionProvider()
}
