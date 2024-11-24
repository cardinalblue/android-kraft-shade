package com.cardinalblue.kraftshade.dsl

import android.graphics.Bitmap
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.TextureBufferPool
import com.cardinalblue.kraftshade.pipeline.input.Input
import com.cardinalblue.kraftshade.pipeline.input.SampledInput
import com.cardinalblue.kraftshade.shader.KraftShader
import com.cardinalblue.kraftshade.shader.buffer.GlBufferProvider
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture

class GlEnvDslScope(
    val env: GlEnv
) {
    suspend fun use(block: GlEnvDslScope.() -> Unit) {
        env.use {
            block()
        }
    }

    /**
     * @param bufferSize The size (width, height) of the rendering target.
     */
    @PipelineScopeMarker
    suspend fun GlEnvDslScope.pipeline(
        bufferSize: GlSize,
        automaticRecycle: Boolean = true,
        block: suspend PipelineSetupScope.() -> Unit = {},
    ): Pipeline {
        return env.use {
            val pipeline = Pipeline(env, TextureBufferPool(bufferSize), automaticRecycle)
            val scope = PipelineSetupScope(pipeline)
            scope.block()
            pipeline
        }
    }

    @PipelineScopeMarker
    suspend fun GlEnvDslScope.pipeline(
        bufferWidth: Int,
        bufferHeight: Int,
        automaticRecycle: Boolean = true,
        block: suspend PipelineSetupScope.() -> Unit = {},
    ): Pipeline {
        return pipeline(GlSize(bufferWidth, bufferHeight), automaticRecycle, block)
    }

    suspend fun terminateEnv() {
        env.use {
            env.terminate()
        }
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

    fun Bitmap.asTexture() = LoadedTexture(this)
}
