package com.cardinalblue.kraftshade.dsl

import android.graphics.BitmapFactory
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.serialization.SerializedEffect
import com.cardinalblue.kraftshade.pipeline.TextureBufferPool
import com.cardinalblue.kraftshade.shader.buffer.GlBuffer
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture

@KraftShadeDsl
open class GlEnvDslScope(
    val env: GlEnv
) {
    @KraftShadeDsl
    suspend fun use(block: GlEnvDslScope.() -> Unit) {
        env.execute {
            block()
        }
    }

    /**
     * @param bufferSize The size (width, height) of the rendering target.
     */
    @KraftShadeDsl
    suspend fun pipeline(
        targetBuffer: GlBuffer,
        automaticRecycle: Boolean = true,
        block: suspend GraphPipelineSetupScope.() -> Unit = {},
    ): Pipeline {
        return env.execute {
            val pipeline = Pipeline(env, TextureBufferPool(targetBuffer.size), automaticRecycle)
            val scope = GraphPipelineSetupScope(env, pipeline, targetBuffer)
            scope.block()
            pipeline
        }
    }

    @KraftShadeDsl
    suspend fun pipeline(
        targetBuffer: GlBuffer,
        serializedEffect: SerializedEffect,
        automaticRecycle: Boolean = true,
    ): Pipeline {
        return env.execute {
            val pipeline = Pipeline(env, TextureBufferPool(targetBuffer.size), automaticRecycle)
            serializedEffect.applyTo(pipeline, targetBuffer)
            pipeline
        }
    }

    suspend fun loadAssetTexture(assetPath: String): LoadedTexture {
        val bitmap = env.appContext.assets.open(assetPath).use {
            BitmapFactory.decodeStream(it)
        }

        return env.execute {
            LoadedTexture(bitmap, name = assetPath)
        }
    }

    suspend fun terminateEnv() {
        env.execute {
            env.terminate()
        }
    }
}
