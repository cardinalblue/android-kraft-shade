package com.cardinalblue.kraftshade.dsl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.Pipeline
import com.cardinalblue.kraftshade.pipeline.TextureBufferPool
import com.cardinalblue.kraftshade.shader.buffer.LoadedTexture

@KraftShadeDsl
class GlEnvDslScope(
    val env: GlEnv
) {
    suspend fun use(block: GlEnvDslScope.() -> Unit) {
        env.execute {
            block()
        }
    }

    /**
     * @param bufferSize The size (width, height) of the rendering target.
     */
    suspend fun pipeline(
        bufferSize: GlSize,
        automaticRecycle: Boolean = true,
        block: suspend PipelineSetupScope.() -> Unit = {},
    ): Pipeline {
        return env.execute {
            val pipeline = Pipeline(env, TextureBufferPool(bufferSize), automaticRecycle)
            val scope = PipelineSetupScope(pipeline)
            scope.block()
            pipeline
        }
    }

    suspend fun pipeline(
        bufferWidth: Int,
        bufferHeight: Int,
        automaticRecycle: Boolean = true,
        block: suspend PipelineSetupScope.() -> Unit = {},
    ): Pipeline {
        return pipeline(GlSize(bufferWidth, bufferHeight), automaticRecycle, block)
    }

    suspend fun loadAssetTexture(assetPath: String): LoadedTexture {
        val bitmap = env.appContext.assets.open(assetPath).use {
            BitmapFactory.decodeStream(it)
        }

        return env.execute {
            LoadedTexture(bitmap)
        }
    }

    suspend fun terminateEnv() {
        env.execute {
            env.terminate()
        }
    }
}
