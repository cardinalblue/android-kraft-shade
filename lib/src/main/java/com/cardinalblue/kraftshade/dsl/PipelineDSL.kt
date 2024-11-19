package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.Effect
import com.cardinalblue.kraftshade.pipeline.SerialTextureInputPipeline
import com.cardinalblue.kraftshade.pipeline.SingleInputTextureEffect
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.shader.KraftShader

@DslMarker
annotation class PipelineScopeMarker

@PipelineScopeMarker
suspend fun GlEnv.serialTextureInputPipeline(
    effects: List<SingleInputTextureEffect> = emptyList(),
    block: SerialTextureInputPipelineScope.() -> Unit = {},
): SerialTextureInputPipeline {
    return use {
        val pipeline = SerialTextureInputPipeline(this, effects)
        block(SerialTextureInputPipelineScope(pipeline))
        pipeline
    }
}

class SerialTextureInputPipelineScope(
    private val pipeline: SerialTextureInputPipeline
) {
    @PipelineScopeMarker
    operator fun SingleInputTextureEffect.unaryPlus() {
        pipeline.addEffect(this)
    }

    @PipelineScopeMarker
    fun <T : Any, IN : Input<T>> SingleInputTextureEffect.withInput(
        input: IN,
        sampledFromExternal: Boolean = false,
        action: (IN, SingleInputTextureEffect) -> Unit
    ): SingleInputTextureEffect {
        connectInput(input, this@withInput, sampledFromExternal, action)
        return this
    }

    @PipelineScopeMarker
    operator fun <S : SingleInputTextureEffect> (() -> S).unaryPlus() {
        +this()
    }

    @PipelineScopeMarker
    operator fun SingleInputTextureEffect.unaryMinus() {
        pipeline.removeEffect(this)
    }

    @PipelineScopeMarker
    fun <S : SingleInputTextureEffect> effect(block: () -> S) {
        pipeline.addEffect(block())
    }

    @PipelineScopeMarker
    fun <T : Any, IN : Input<T>, E : Effect> connectInput(
        input: IN,
        effect: E,
        sampledFromExternal: Boolean = false,
        action: (IN, E) -> Unit
    ) {
        pipeline.connectInput(input, effect, sampledFromExternal, action)
    }
}