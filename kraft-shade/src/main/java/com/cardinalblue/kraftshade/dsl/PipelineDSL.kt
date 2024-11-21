package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.pipeline.Effect
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.SerialTextureInputPipeline
import com.cardinalblue.kraftshade.pipeline.SingleInputTextureEffect
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.TimeInput
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.Texture

@DslMarker
annotation class PipelineScopeMarker

open class PipelineScope<P : Pipeline>(
    protected val pipeline: P,
) {
    @PipelineScopeMarker
    fun withPipeline(block: P.() -> Unit) {
        block(pipeline)
    }

    @PipelineScopeMarker
    fun <T : Any, IN : Input<T>, E : Effect> E.withInput(
        input: IN,
        sampledFromExternal: Boolean = false,
        action: (IN, E) -> Unit
    ): E {
        connectInput(input, this@withInput, sampledFromExternal, action)
        return this
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

    @PipelineScopeMarker
    fun setTargetBuffer(buffer: GlBuffer) {
        pipeline.setTargetBuffer(buffer)
    }
}

class SerialTextureInputPipelineScope(
    pipeline: SerialTextureInputPipeline
) : PipelineScope<SerialTextureInputPipeline>(pipeline) {
    @PipelineScopeMarker
    fun setInputTexture(texture: Texture) {
        pipeline.setInputTexture(texture)
    }

    @PipelineScopeMarker
    operator fun SingleInputTextureEffect.unaryPlus() {
        pipeline.addEffect(this)
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
}

object CommonInputs {
    fun time(start: Boolean = true) = TimeInput().apply {
        if (start) start()
    }
}