package com.cardinalblue.kraftshade.dsl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.EffectExecutionProvider
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

suspend fun kraftBitmap(
    context: Context,
    outputWidth: Int,
    outputHeight: Int,
    block: suspend KraftBitmapDslScope.() -> Bitmap
) : Bitmap {
    return GlEnv(context).use {
        env.execute {
            val scope = KraftBitmapDslScope(outputWidth, outputHeight, env)
            scope.block()
        }
    }
}

@KraftShadeDsl
class KraftBitmapDslScope(
    private val outputWidth: Int,
    private val outputHeight: Int,
    env: GlEnv
) {
    private val envScope = GlEnvDslScope(env)

    suspend fun withPipeline(
        block: suspend PipelineSetupScope.(outputBuffer: TextureBuffer) -> Unit = {},
    ): Bitmap {
        with(envScope) {
            val outputBuffer = TextureBuffer(
                this@KraftBitmapDslScope.outputWidth,
                this@KraftBitmapDslScope.outputHeight
            )
            pipeline(
                this@KraftBitmapDslScope.outputWidth,
                this@KraftBitmapDslScope.outputHeight,
                block = { block(outputBuffer) }
            ).run()
            return outputBuffer.getBitmap()
        }
    }
}

suspend fun kraftBitmapFrom(
    context: Context,
    inputBitmap: Bitmap,
    block: suspend KraftBitmapWithInputDslScope.() -> Bitmap
): Bitmap {
    return GlEnv(context).use {
        env.execute {
            val scope = KraftBitmapWithInputDslScope(inputBitmap, env)
            scope.block()
        }
    }
}

suspend fun kraftBitmapFrom(
    context: Context,
    inputSize: GlSize,
    effectExecutionProvider: EffectExecutionProvider,
): Bitmap {
    return GlEnv(context).use {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val targetBuffer = TextureBuffer(inputSize)
        effectExecutionProvider(this, targetBuffer).run()
        targetBuffer.getBitmap()
    }
}

@KraftShadeDsl
class KraftBitmapWithInputDslScope(
    private val input: Bitmap,
    val env: GlEnv,
) {
    suspend fun withPipeline(
        block: suspend PipelineSetupScope.(inputTexture: Texture, outputBuffer: GlBuffer) -> Unit,
    ): Bitmap {
        val kraftBitmapDslScope = KraftBitmapDslScope(input.width, input.height, env)
        with(kraftBitmapDslScope) {
            return withPipeline { outputBuffer ->
                val inputTexture = LoadedTexture(this@KraftBitmapWithInputDslScope.input)
                block(inputTexture, outputBuffer)
            }
        }
    }

    suspend fun serialPipeline(
        block: suspend PipelineShaderBuilderScope.() -> Unit,
    ): Bitmap = withPipeline { inputTexture, outputBuffer ->
        serialSteps(inputTexture, outputBuffer) {
            val shaders = PipelineShaderBuilderScope().run {
                block()
                build()
            }

            shaders.forEach { shader ->
                // no need to setup at all since everything should be already configured on the
                // shaders already
                step(shader)
            }
        }
    }
}