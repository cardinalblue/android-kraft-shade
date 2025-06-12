package com.cardinalblue.kraftshade.dsl

import android.content.Context
import android.graphics.Bitmap
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.AnimatedEffectExecutionProvider
import com.cardinalblue.kraftshade.pipeline.EffectExecutionProvider
import com.cardinalblue.kraftshade.pipeline.input.constInput
import com.cardinalblue.kraftshade.shader.TextureInputKraftShader
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture
import com.cardinalblue.kraftshade.shader.buffer.Texture
import com.cardinalblue.kraftshade.shader.buffer.TextureBuffer

suspend fun TextureInputKraftShader.apply(
    context: Context,
    inputBitmap: Bitmap,
): Bitmap {
    return kraftBitmap(context, inputBitmap) {
        serialPipeline {
            addShader { this@apply }
        }
    }
}

suspend fun kraftBitmap(
    context: Context,
    inputBitmap: Bitmap,
    vararg shaders: TextureInputKraftShader
): Bitmap {
    return kraftBitmap(context, inputBitmap) {
        serialPipeline {
            shaders.forEach {
                addShader { it }
            }
        }
    }
}

suspend fun kraftBitmap(
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

suspend fun EffectExecutionProvider.kraftBitmap(
    context: Context,
    targetSize: GlSize,
): Bitmap {
    return GlEnv(context).use {
        val targetBuffer = TextureBuffer(targetSize)
        provide(targetBuffer).run()
        targetBuffer.getBitmap()
    }
}

suspend fun AnimatedEffectExecutionProvider.kraftBitmap(
    context: Context,
    targetSize: GlSize,
    time: Float = 0f,
): Bitmap {
    return GlEnv(context).use {
        val targetBuffer = TextureBuffer(targetSize)
        provide(targetBuffer, constInput(time)).run()
        targetBuffer.getBitmap()
    }
}

@KraftShadeDsl
class KraftBitmapWithInputDslScope(
    private val input: Bitmap,
    val env: GlEnv,
) {
    suspend fun withPipeline(
        block: suspend GraphPipelineSetupScope.(inputTexture: Texture) -> Unit,
    ): Bitmap {
        val kraftBitmapDslScope = KraftBitmapDslScope(input.width, input.height, env)
        with(kraftBitmapDslScope) {
            return withPipeline {
                val inputTexture = LoadedTexture(this@KraftBitmapWithInputDslScope.input)
                block(inputTexture)
            }
        }
    }

    suspend fun serialPipeline(
        block: suspend SimpleSerialPipelineBuilderScope.() -> Unit,
    ): Bitmap = withPipeline { inputTexture ->
        serialSteps(inputTexture, graphTargetBuffer) {
            val shaders = SimpleSerialPipelineBuilderScope().run {
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

@KraftShadeDsl
class KraftBitmapDslScope(
    private val outputWidth: Int,
    private val outputHeight: Int,
    env: GlEnv
) {
    private val envScope = GlEnvDslScope(env)

    suspend fun withPipeline(
        block: suspend GraphPipelineSetupScope.() -> Unit = {},
    ): Bitmap {
        with(envScope) {
            val outputBuffer = TextureBuffer(
                this@KraftBitmapDslScope.outputWidth,
                this@KraftBitmapDslScope.outputHeight
            )
            pipeline(
                outputBuffer,
                block = block,
            ).run()
            return outputBuffer.getBitmap()
        }
    }
}

@KraftShadeDsl
class SimpleSerialPipelineBuilderScope {
    private val shaders: MutableList<TextureInputKraftShader> = mutableListOf()

    suspend fun addShader(block: suspend () -> TextureInputKraftShader) {
        shaders.add(block())
    }

    fun build(): List<TextureInputKraftShader> {
        return shaders
    }
}
