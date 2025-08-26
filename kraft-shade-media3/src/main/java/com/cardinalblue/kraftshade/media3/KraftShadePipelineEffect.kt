package com.cardinalblue.kraftshade.media3

import android.content.Context
import android.opengl.EGL14
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import com.cardinalblue.kraftshade.env.GlEnv
import com.cardinalblue.kraftshade.model.GlSize
import com.cardinalblue.kraftshade.pipeline.EffectExecution
import com.cardinalblue.kraftshade.pipeline.input.sampledInput
import com.cardinalblue.kraftshade.shader.buffer.ExternalFrameBuffer
import com.cardinalblue.kraftshade.shader.buffer.IdPassingTexture
import kotlinx.coroutines.runBlocking

@UnstableApi
class KraftShadePipelineEffect(
    context: Context,
    provider: VideoEffectExecutionProvider
) : GlEffect {
    private val program by lazy { KraftShadePipelineShaderProgram(context, provider) }
    override fun toGlShaderProgram(
        context: Context,
        useHdr: Boolean
    ): GlShaderProgram = program
}

@UnstableApi
class KraftShadePipelineShaderProgram(
    private val context: Context,
    private val provider: VideoEffectExecutionProvider,
) : BaseGlShaderProgram(true, 1) {
    private lateinit var glEnv: GlEnv
    private val texture = IdPassingTexture()
    private val externalFrameBuffer = ExternalFrameBuffer(false)
    private var effectExecution: EffectExecution? = null

    private var presentationTime: Float = 0f
    private val presentationTimeInput = sampledInput { presentationTime }

    private fun init() {
        if (effectExecution != null) return
        glEnv = GlEnv(context, useUnconfinedDispatcher = true, eglContext = EGL14.eglGetCurrentContext())
        externalFrameBuffer.setCurrentFrameBuffer()
        check(externalFrameBuffer.isValid()) {
            "External frame buffer is not valid. Make sure configure() is called before drawFrame()."
        }
        runBlocking {
            effectExecution = with(provider) {
                glEnv.execute {
                    provide(
                        glBuffer = externalFrameBuffer,
                        presentationTimeInput = presentationTimeInput,
                        videoTexture = texture
                    )
                }
            }
        }
    }

    override fun configure(
        inputWidth: Int,
        inputHeight: Int
    ): Size {
        val newGlSize = GlSize(inputWidth, inputHeight)
        val effectExecution = effectExecution
        if (effectExecution != null) {
            runBlocking {
                effectExecution.onBufferSizeChanged(newGlSize)
            }
        }
        externalFrameBuffer.size = newGlSize
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        init()
        presentationTime = presentationTimeUs / 1_000_000f
        val effectExecution = requireNotNull(effectExecution) {
            "effectExecution is not initialized"
        }
        texture.setId(inputTexId)
        runBlocking {
            effectExecution.run()
        }
    }
}
