package com.cardinalblue.kraftshade.dsl

import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.SerialTextureInputPipeline
import com.cardinalblue.kraftshade.pipeline.SingleInputTextureEffect
import com.cardinalblue.kraftshade.pipeline.WrapperPipeline
import com.cardinalblue.kraftshade.shader.KraftShader

class GlEnvDslScope(
    val env: GlEnv
) {
    suspend fun <T : KraftShader> T.asPipeline(
        setup: suspend PipelineScope<Pipeline>.(T) -> Unit = {},
    ): Pipeline {
        val pipeline = WrapperPipeline(env, this)
        PipelineScope<Pipeline>(pipeline).apply {
            setup(this@asPipeline)
        }
        return pipeline
    }

    @PipelineScopeMarker
    suspend fun GlEnvDslScope.serialTextureInputPipeline(
        effects: List<SingleInputTextureEffect> = emptyList(),
        block: SerialTextureInputPipelineScope.() -> Unit = {},
    ): SerialTextureInputPipeline {
        return env.use {
            val pipeline = SerialTextureInputPipeline(env, effects)
            block(SerialTextureInputPipelineScope(pipeline))
            pipeline
        }
    }

    suspend fun terminateEnv() {
        env.terminate()
    }
}
