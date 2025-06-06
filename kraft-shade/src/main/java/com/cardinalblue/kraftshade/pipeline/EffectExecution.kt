package com.cardinalblue.kraftshade.pipeline

import com.cardinalblue.kraftshade.dsl.GlEnvDslScope
import com.cardinalblue.kraftshade.dsl.GraphPipelineSetupScope
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.constInput
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.TextureProvider

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

fun interface EffectExecutionProvider {
    suspend fun GlEnvDslScope.provide(glBuffer: GlBuffer): EffectExecution
}

fun interface AnimatedEffectExecutionProvider {
    suspend fun GlEnvDslScope.provide(glBuffer: GlBuffer, timeInput: Input<Float>): EffectExecution

    fun withTime(time: Float): EffectExecutionProvider = EffectExecutionProvider { glBuffer ->
        provide(glBuffer, constInput(time))
    }
}

fun createEffectExecutionProviderWithPipeline(
    block: suspend GraphPipelineSetupScope.() -> Unit = {}
) = EffectExecutionProvider { targetBuffer ->
    pipeline(targetBuffer) { block() }
}

fun createEffectExecutionProviderByJson(
    json: String,
    textures: Map<String, TextureProvider> = emptyMap(),
) = EffectExecutionProvider { targetBuffer ->
    pipeline(
        targetBuffer = targetBuffer,
        json = json,
        textures = textures,
    )
}

fun createAnimatedEffectExecutionProviderWithPipeline(
    block: suspend GraphPipelineSetupScope.(timeInput: Input<Float>) -> Unit = {},
) = AnimatedEffectExecutionProvider { targetBuffer, timeInput ->
    pipeline(targetBuffer) { block(timeInput) }
}

fun TextureInputKraftShader.asEffectExecutionProvider(
    inputTextureProvider: TextureProvider
) = createEffectExecutionProviderWithPipeline {
    step(
        this@asEffectExecutionProvider,
        targetBuffer = graphTargetBuffer
    ) {
        setInputTexture(inputTextureProvider)
    }
}
