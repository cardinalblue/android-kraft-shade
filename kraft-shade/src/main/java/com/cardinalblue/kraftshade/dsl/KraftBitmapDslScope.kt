package com.cardinalblue.kraftshade.dsl

import android.graphics.Bitmap
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

@DslMarker
annotation class KraftBitmapScopeMarker

suspend fun kraftBitmap(
    outputWidth: Int,
    outputHeight: Int,
    block: suspend KraftBitmapDslScope.() -> Bitmap
) : Bitmap {
    return GlEnv().use {
        env.execute {
            val scope = KraftBitmapDslScope(outputWidth, outputHeight, env)
            scope.block()
        }
    }
}

@KraftBitmapScopeMarker
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
            val outputBuffer = TextureBuffer(outputWidth, outputHeight)
            pipeline(outputWidth, outputHeight, block = { block(outputBuffer) })
                .run()
            return outputBuffer.getBitmap()
        }
    }
}

suspend fun kraftBitmapFrom(
    inputBitmap: Bitmap,
    block: suspend KraftBitmapWithInputDslScope.() -> Bitmap
): Bitmap {
    return GlEnv().use {
        env.execute {
            val scope = KraftBitmapWithInputDslScope(inputBitmap, env)
            scope.block()
        }
    }
}

@KraftBitmapScopeMarker
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